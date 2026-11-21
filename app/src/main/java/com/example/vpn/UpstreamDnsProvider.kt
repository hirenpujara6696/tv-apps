package com.example.vpn

enum class UpstreamDnsProvider(
    val id: String,
    val displayName: String,
    val primaryIp: String,
    val secondaryIp: String,
    val badge: String,
    val descriptionEn: String,
    val descriptionHi: String
) {
    ADGUARD(
        id = "adguard",
        displayName = "AdGuard DNS",
        primaryIp = "94.140.14.14",
        secondaryIp = "94.140.15.15",
        badge = "BEST FOR TV",
        descriptionEn = "Double-layer ad & tracker blocking at DNS level",
        descriptionHi = "DNS स्तर पर डबल-लेयर विज्ञापन और ट्रैकर ब्लॉकिंग"
    ),
    MULLVAD(
        id = "mullvad",
        displayName = "Mullvad AdBlock",
        primaryIp = "194.242.2.3",
        secondaryIp = "94.140.14.14",
        badge = "PRIVACY",
        descriptionEn = "No-logs European DNS with built-in ad & tracker filter",
        descriptionHi = "बिना लॉग वाला प्राइवेसी DNS, इनबिल्ट ऐड फ़िल्टर के साथ"
    ),
    CONTROLD(
        id = "controld",
        displayName = "Control D Ads",
        primaryIp = "76.76.2.2",
        secondaryIp = "76.76.10.2",
        badge = "FAST",
        descriptionEn = "Low-latency ad & malware blocking DNS for streaming",
        descriptionHi = "स्ट्रीमिंग के लिए तेज़ ऐड और मैलवेयर ब्लॉकिंग DNS"
    ),
    CLOUDFLARE(
        id = "cloudflare",
        displayName = "Cloudflare 1.1.1.1",
        primaryIp = "1.1.1.1",
        secondaryIp = "1.0.0.1",
        badge = "ULTRA FAST",
        descriptionEn = "Fastest global DNS (uses local TV Shield rules only)",
        descriptionHi = "सबसे तेज़ DNS (केवल लोकल TV Shield नियम उपयोग करता है)"
    ),
    QUAD9(
        id = "quad9",
        displayName = "Quad9 Security",
        primaryIp = "9.9.9.9",
        secondaryIp = "149.112.112.112",
        badge = "SECURITY",
        descriptionEn = "Blocks phishing & malicious domains + local TV Shield rules",
        descriptionHi = "फ़िशिंग और खतरनाक डोमेन ब्लॉक करता है"
    );

    companion object {
        fun fromId(id: String): UpstreamDnsProvider {
            return entries.firstOrNull { it.id == id } ?: ADGUARD
        }
    }
}
