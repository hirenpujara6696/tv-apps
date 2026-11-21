package com.example.vpn

import java.io.ByteArrayOutputStream

data class ParsedDnsRequest(
    val ipHeaderLength: Int,
    val srcIp: ByteArray,
    val dstIp: ByteArray,
    val srcPort: Int,
    val dstPort: Int,
    val dnsPayload: ByteArray,
    val domain: String,
    val queryType: Int,
    val questionEndOffset: Int
)

object DnsPacketCodec {

    private const val PROTOCOL_UDP = 17
    private const val DNS_PORT = 53
    private const val QTYPE_A = 1
    private const val QTYPE_AAAA = 28

    fun parseIpv4UdpDnsRequest(packet: ByteArray, length: Int): ParsedDnsRequest? {
        if (length < 28) return null
        val version = (packet[0].toInt() ushr 4) and 0x0F
        if (version != 4) return null

        val ihl = (packet[0].toInt() and 0x0F) * 4
        if (ihl < 20 || length < ihl + 8 + 12) return null

        val protocol = packet[9].toInt() and 0xFF
        if (protocol != PROTOCOL_UDP) return null

        val srcPort = ((packet[ihl].toInt() and 0xFF) shl 8) or (packet[ihl + 1].toInt() and 0xFF)
        val dstPort = ((packet[ihl + 2].toInt() and 0xFF) shl 8) or (packet[ihl + 3].toInt() and 0xFF)
        if (dstPort != DNS_PORT) return null

        val udpLength = ((packet[ihl + 4].toInt() and 0xFF) shl 8) or (packet[ihl + 5].toInt() and 0xFF)
        val dnsOffset = ihl + 8
        val dnsLength = minOf(udpLength - 8, length - dnsOffset)
        if (dnsLength < 12) return null

        val dnsPayload = packet.copyOfRange(dnsOffset, dnsOffset + dnsLength)
        val parsedQuestion = parseQuestion(dnsPayload) ?: return null

        return ParsedDnsRequest(
            ipHeaderLength = ihl,
            srcIp = packet.copyOfRange(12, 16),
            dstIp = packet.copyOfRange(16, 20),
            srcPort = srcPort,
            dstPort = dstPort,
            dnsPayload = dnsPayload,
            domain = parsedQuestion.domain,
            queryType = parsedQuestion.qType,
            questionEndOffset = parsedQuestion.questionEndOffset
        )
    }

    private data class ParsedQuestion(
        val domain: String,
        val qType: Int,
        val questionEndOffset: Int
    )

    private fun parseQuestion(dns: ByteArray): ParsedQuestion? {
        if (dns.size < 12) return null
        val qdCount = ((dns[4].toInt() and 0xFF) shl 8) or (dns[5].toInt() and 0xFF)
        if (qdCount < 1) return null

        val labels = mutableListOf<String>()
        var offset = 12
        var guard = 0
        while (offset < dns.size && guard < 64) {
            guard++
            val labelLen = dns[offset].toInt() and 0xFF
            if (labelLen == 0) {
                offset++
                break
            }
            if ((labelLen and 0xC0) == 0xC0) {
                // Pointer not expected in standard question, bail safely
                offset += 2
                break
            }
            offset++
            if (offset + labelLen > dns.size) return null
            val label = String(dns, offset, labelLen, Charsets.US_ASCII)
            labels.add(label)
            offset += labelLen
        }

        if (offset + 4 > dns.size) return null
        val qType = ((dns[offset].toInt() and 0xFF) shl 8) or (dns[offset + 1].toInt() and 0xFF)
        val questionEndOffset = offset + 4
        val domain = labels.joinToString(".")
        return ParsedQuestion(domain = domain, qType = qType, questionEndOffset = questionEndOffset)
    }

    fun buildBlockedDnsPayload(request: ParsedDnsRequest, zeroIpMode: Boolean): ByteArray {
        val query = request.dnsPayload
        val qEnd = request.questionEndOffset.coerceAtMost(query.size)
        val out = ByteArrayOutputStream(qEnd + 32)

        val rdBit = query[2].toInt() and 0x01
        val respondWithZeroIp = zeroIpMode && (request.queryType == QTYPE_A || request.queryType == QTYPE_AAAA)

        // Transaction ID
        out.write(query[0].toInt() and 0xFF)
        out.write(query[1].toInt() and 0xFF)

        // Flags: QR=1, Opcode=0, AA=0, TC=0, RD=copied
        out.write(0x80 or rdBit)
        // RA=1, Z=0, RCODE = 0 (NOERROR) for 0.0.0.0 or 3 (NXDOMAIN)
        out.write(if (respondWithZeroIp) 0x80 else 0x83)

        // QDCOUNT = 1
        out.write(0x00)
        out.write(0x01)
        // ANCOUNT = 1 if zeroIp else 0
        out.write(0x00)
        out.write(if (respondWithZeroIp) 0x01 else 0x00)
        // NSCOUNT = 0, ARCOUNT = 0
        out.write(0x00)
        out.write(0x00)
        out.write(0x00)
        out.write(0x00)

        // Original Question section
        out.write(query, 12, qEnd - 12)

        if (respondWithZeroIp) {
            // Name pointer to offset 12 (0xC00C)
            out.write(0xC0)
            out.write(0x0C)
            // TYPE
            out.write((request.queryType ushr 8) and 0xFF)
            out.write(request.queryType and 0xFF)
            // CLASS IN (1)
            out.write(0x00)
            out.write(0x01)
            // TTL = 60 seconds
            out.write(0x00)
            out.write(0x00)
            out.write(0x00)
            out.write(0x3C)

            if (request.queryType == QTYPE_A) {
                // RDLENGTH = 4, RDATA = 0.0.0.0
                out.write(0x00)
                out.write(0x04)
                out.write(0x00)
                out.write(0x00)
                out.write(0x00)
                out.write(0x00)
            } else {
                // RDLENGTH = 16, RDATA = ::
                out.write(0x00)
                out.write(0x10)
                repeat(16) { out.write(0x00) }
            }
        }

        return out.toByteArray()
    }

    fun buildIpv4UdpResponsePacket(
        request: ParsedDnsRequest,
        dnsResponsePayload: ByteArray
    ): ByteArray {
        val ipHeaderLen = 20
        val udpLen = 8 + dnsResponsePayload.size
        val totalLen = ipHeaderLen + udpLen
        val packet = ByteArray(totalLen)

        // IPv4 Header
        packet[0] = 0x45.toByte() // Version 4, IHL 5 (20 bytes)
        packet[1] = 0x00
        packet[2] = ((totalLen ushr 8) and 0xFF).toByte()
        packet[3] = (totalLen and 0xFF).toByte()
        // Identification
        packet[4] = request.dnsPayload[0]
        packet[5] = request.dnsPayload[1]
        // Flags + Fragment Offset (Don't Fragment)
        packet[6] = 0x40.toByte()
        packet[7] = 0x00
        // TTL = 64
        packet[8] = 64.toByte()
        // Protocol = UDP (17)
        packet[9] = PROTOCOL_UDP.toByte()
        // Checksum placeholder
        packet[10] = 0
        packet[11] = 0
        // Source IP = original Destination IP (10.120.0.2)
        System.arraycopy(request.dstIp, 0, packet, 12, 4)
        // Destination IP = original Source IP (10.120.0.1)
        System.arraycopy(request.srcIp, 0, packet, 16, 4)

        // Compute IPv4 header checksum
        val checksum = computeIpv4Checksum(packet, ipHeaderLen)
        packet[10] = ((checksum ushr 8) and 0xFF).toByte()
        packet[11] = (checksum and 0xFF).toByte()

        // UDP Header (swap src/dst ports)
        packet[20] = ((request.dstPort ushr 8) and 0xFF).toByte()
        packet[21] = (request.dstPort and 0xFF).toByte()
        packet[22] = ((request.srcPort ushr 8) and 0xFF).toByte()
        packet[23] = (request.srcPort and 0xFF).toByte()
        packet[24] = ((udpLen ushr 8) and 0xFF).toByte()
        packet[25] = (udpLen and 0xFF).toByte()
        // UDP checksum 0 is optional/valid for IPv4
        packet[26] = 0
        packet[27] = 0

        // DNS Payload
        System.arraycopy(dnsResponsePayload, 0, packet, 28, dnsResponsePayload.size)
        return packet
    }

    private fun computeIpv4Checksum(header: ByteArray, length: Int): Int {
        var sum = 0
        var i = 0
        while (i < length) {
            val high = header[i].toInt() and 0xFF
            val low = if (i + 1 < length) header[i + 1].toInt() and 0xFF else 0
            sum += (high shl 8) or low
            while ((sum ushr 16) != 0) {
                sum = (sum and 0xFFFF) + (sum ushr 16)
            }
            i += 2
        }
        return sum.inv() and 0xFFFF
    }
}
