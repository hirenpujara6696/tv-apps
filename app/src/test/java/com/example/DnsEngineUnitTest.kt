package com.example

import com.example.data.CustomDomainRule
import com.example.data.ShieldSettings
import com.example.vpn.DnsPacketCodec
import com.example.vpn.DomainFilterEngine
import com.example.vpn.FilterCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream

class DnsEngineUnitTest {

    @Test
    fun domainFilterEngine_blocksSubdomainsAndRespectsAllowlist() {
        val engine = DomainFilterEngine()
        engine.updateConfiguration(
            settings = ShieldSettings(
                blockTvAds = true,
                blockAdNetworks = true,
                blockStreamingAds = true,
                blockTrackers = true
            ),
            customRules = listOf(
                CustomDomainRule(domain = "googleads.g.doubleclick.net", isAllowlist = true),
                CustomDomainRule(domain = "custom-tv-ad.in", isAllowlist = false)
            )
        )

        val blockedSub = engine.evaluate("sub.doubleclick.net")
        assertTrue(blockedSub.blocked)
        assertEquals(FilterCategory.AD_NETWORKS, blockedSub.category)

        val whitelisted = engine.evaluate("googleads.g.doubleclick.net")
        assertFalse(whitelisted.blocked)
        assertEquals(FilterCategory.CUSTOM_ALLOW, whitelisted.category)

        val customBlocked = engine.evaluate("banner.custom-tv-ad.in")
        assertTrue(customBlocked.blocked)
        assertEquals(FilterCategory.CUSTOM_BLOCK, customBlocked.category)

        val cleanDomain = engine.evaluate("netflix.com")
        assertFalse(cleanDomain.blocked)
        assertEquals(FilterCategory.ALLOWED, cleanDomain.category)
    }

    @Test
    fun dnsPacketCodec_parsesIpv4UdpQueryAndBuildsZeroIpReply() {
        val dnsQuery = buildSampleDnsQuery("ads.sansui.tv", qType = 1)
        val ipUdpPacket = wrapIpv4UdpPacket(
            srcIp = byteArrayOf(10, 120, 0, 1),
            dstIp = byteArrayOf(10, 120, 0, 2),
            srcPort = 51234,
            dstPort = 53,
            payload = dnsQuery
        )

        val parsed = DnsPacketCodec.parseIpv4UdpDnsRequest(ipUdpPacket, ipUdpPacket.size)
        assertNotNull(parsed)
        assertEquals("ads.sansui.tv", parsed!!.domain)
        assertEquals(1, parsed.queryType)
        assertEquals(51234, parsed.srcPort)
        assertEquals(53, parsed.dstPort)

        val blockedPayload = DnsPacketCodec.buildBlockedDnsPayload(parsed, zeroIpMode = true)
        // QR=1, ANCOUNT=1
        assertTrue((blockedPayload[2].toInt() and 0x80) != 0)
        assertEquals(1, blockedPayload[7].toInt() and 0xFF)
        // Last 4 bytes must be 0.0.0.0
        val len = blockedPayload.size
        assertEquals(0, blockedPayload[len - 4].toInt())
        assertEquals(0, blockedPayload[len - 3].toInt())
        assertEquals(0, blockedPayload[len - 2].toInt())
        assertEquals(0, blockedPayload[len - 1].toInt())

        val responsePacket = DnsPacketCodec.buildIpv4UdpResponsePacket(parsed, blockedPayload)
        // Swapped ports: src=53, dst=51234
        val respSrcPort = ((responsePacket[20].toInt() and 0xFF) shl 8) or (responsePacket[21].toInt() and 0xFF)
        val respDstPort = ((responsePacket[22].toInt() and 0xFF) shl 8) or (responsePacket[23].toInt() and 0xFF)
        assertEquals(53, respSrcPort)
        assertEquals(51234, respDstPort)
    }

    private fun buildSampleDnsQuery(domain: String, qType: Int): ByteArray {
        val out = ByteArrayOutputStream()
        // Transaction ID
        out.write(0x12)
        out.write(0x34)
        // Flags: standard query, RD=1
        out.write(0x01)
        out.write(0x00)
        // QDCOUNT = 1, ANCOUNT/NSCOUNT/ARCOUNT = 0
        out.write(0x00); out.write(0x01)
        out.write(0x00); out.write(0x00)
        out.write(0x00); out.write(0x00)
        out.write(0x00); out.write(0x00)
        // QNAME
        domain.split('.').forEach { label ->
            out.write(label.length)
            out.write(label.toByteArray(Charsets.US_ASCII))
        }
        out.write(0x00)
        // QTYPE & QCLASS IN
        out.write((qType ushr 8) and 0xFF)
        out.write(qType and 0xFF)
        out.write(0x00)
        out.write(0x01)
        return out.toByteArray()
    }

    private fun wrapIpv4UdpPacket(
        srcIp: ByteArray,
        dstIp: ByteArray,
        srcPort: Int,
        dstPort: Int,
        payload: ByteArray
    ): ByteArray {
        val udpLen = 8 + payload.size
        val totalLen = 20 + udpLen
        val packet = ByteArray(totalLen)
        packet[0] = 0x45.toByte()
        packet[2] = ((totalLen ushr 8) and 0xFF).toByte()
        packet[3] = (totalLen and 0xFF).toByte()
        packet[8] = 64.toByte()
        packet[9] = 17.toByte()
        System.arraycopy(srcIp, 0, packet, 12, 4)
        System.arraycopy(dstIp, 0, packet, 16, 4)
        packet[20] = ((srcPort ushr 8) and 0xFF).toByte()
        packet[21] = (srcPort and 0xFF).toByte()
        packet[22] = ((dstPort ushr 8) and 0xFF).toByte()
        packet[23] = (dstPort and 0xFF).toByte()
        packet[24] = ((udpLen ushr 8) and 0xFF).toByte()
        packet[25] = (udpLen and 0xFF).toByte()
        System.arraycopy(payload, 0, packet, 28, payload.size)
        return packet
    }
}
