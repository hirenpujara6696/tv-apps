package com.example.vpn

import com.example.data.CustomDomainRule
import com.example.data.ShieldSettings

enum class FilterCategory(val key: String) {
    SMART_TV("Smart TV Ads"),
    AD_NETWORKS("Ad Networks"),
    STREAMING_ADS("OTT & Video Ads"),
    TRACKERS("Trackers & Popups"),
    CUSTOM_BLOCK("Custom Block"),
    CUSTOM_ALLOW("Whitelisted"),
    ALLOWED("Allowed")
}

data class FilterDecision(
    val blocked: Boolean,
    val category: FilterCategory,
    val matchedRule: String? = null
)

class DomainFilterEngine {

    private data class RuleSnapshot(
        val allowlist: Set<String> = emptySet(),
        val customBlocklist: Set<String> = emptySet(),
        val blockTvAds: Boolean = true,
        val blockAdNetworks: Boolean = true,
        val blockStreamingAds: Boolean = true,
        val blockTrackers: Boolean = true
    )

    @Volatile
    private var snapshot: RuleSnapshot = RuleSnapshot()

    fun updateConfiguration(settings: ShieldSettings, customRules: List<CustomDomainRule>) {
        val allow = HashSet<String>()
        val block = HashSet<String>()
        for (rule in customRules) {
            val normalized = normalizeDomain(rule.domain)
            if (normalized.isNotEmpty()) {
                if (rule.isAllowlist) allow.add(normalized) else block.add(normalized)
            }
        }
        snapshot = RuleSnapshot(
            allowlist = allow,
            customBlocklist = block,
            blockTvAds = settings.blockTvAds,
            blockAdNetworks = settings.blockAdNetworks,
            blockStreamingAds = settings.blockStreamingAds,
            blockTrackers = settings.blockTrackers
        )
    }

    fun evaluate(rawDomain: String): FilterDecision {
        val domain = normalizeDomain(rawDomain)
        if (domain.isEmpty()) {
            return FilterDecision(blocked = false, category = FilterCategory.ALLOWED)
        }

        val current = snapshot

        // 1. User Allowlist (Whitelist) always wins
        matchInSet(domain, current.allowlist)?.let {
            return FilterDecision(blocked = false, category = FilterCategory.CUSTOM_ALLOW, matchedRule = it)
        }

        // 2. User Custom Blocklist
        matchInSet(domain, current.customBlocklist)?.let {
            return FilterDecision(blocked = true, category = FilterCategory.CUSTOM_BLOCK, matchedRule = it)
        }

        // 3. Built-in Smart TV & Google TV OEM Ad Servers
        if (current.blockTvAds) {
            matchInSet(domain, SMART_TV_DOMAINS)?.let {
                return FilterDecision(blocked = true, category = FilterCategory.SMART_TV, matchedRule = it)
            }
        }

        // 4. Built-in Streaming / OTT Pre-roll & Video Ad Servers
        if (current.blockStreamingAds) {
            matchInSet(domain, STREAMING_AD_DOMAINS)?.let {
                return FilterDecision(blocked = true, category = FilterCategory.STREAMING_ADS, matchedRule = it)
            }
        }

        // 5. Built-in Global & Indian Ad Networks
        if (current.blockAdNetworks) {
            matchInSet(domain, AD_NETWORK_DOMAINS)?.let {
                return FilterDecision(blocked = true, category = FilterCategory.AD_NETWORKS, matchedRule = it)
            }
        }

        // 6. Built-in Trackers, Analytics & Popups
        if (current.blockTrackers) {
            matchInSet(domain, TRACKER_DOMAINS)?.let {
                return FilterDecision(blocked = true, category = FilterCategory.TRACKERS, matchedRule = it)
            }
        }

        return FilterDecision(blocked = false, category = FilterCategory.ALLOWED)
    }

    fun activeRuleCount(): Int {
        val current = snapshot
        var count = current.customBlocklist.size
        if (current.blockTvAds) count += SMART_TV_DOMAINS.size
        if (current.blockAdNetworks) count += AD_NETWORK_DOMAINS.size
        if (current.blockStreamingAds) count += STREAMING_AD_DOMAINS.size
        if (current.blockTrackers) count += TRACKER_DOMAINS.size
        return count
    }

    private fun matchInSet(domain: String, ruleSet: Set<String>): String? {
        if (ruleSet.isEmpty()) return null
        var candidate = domain
        while (true) {
            if (ruleSet.contains(candidate)) return candidate
            val dotIndex = candidate.indexOf('.')
            if (dotIndex < 0 || dotIndex >= candidate.length - 1) return null
            candidate = candidate.substring(dotIndex + 1)
        }
    }

    companion object {
        fun normalizeDomain(input: String): String {
            return input.trim()
                .lowercase()
                .removePrefix("https://")
                .removePrefix("http://")
                .substringBefore('/')
                .substringBefore(':')
                .trimEnd('.')
        }

        val SMART_TV_DOMAINS: Set<String> = setOf(
            "ads.sansui.tv",
            "ad.sansui.tv",
            "adservice.google.com",
            "adservice.google.co.in",
            "pagead2.googlesyndication.com",
            "googleads.g.doubleclick.net",
            "pubads.g.doubleclick.net",
            "securepubads.g.doubleclick.net",
            "tpc.googlesyndication.com",
            "partnerad.l.doubleclick.net",
            "ads.youtube.com",
            "ads.samsung.com",
            "samsungads.com",
            "gpm.samsungqbe.com",
            "osb-apps.samsungqbe.com",
            "lgad.cjpowercast.com",
            "ngfts.lge.com",
            "us.ad.lgsmartad.com",
            "ad.lgappstv.com",
            "ads.roku.com",
            "cooper.logs.roku.com",
            "scribe.logs.roku.com",
            "ad.tcl.com",
            "log.tcl.com",
            "ad.xiaomi.com",
            "api.ad.xiaomi.com",
            "sdkconfig.ad.xiaomi.com",
            "sdkconfig.ad.intl.xiaomi.com",
            "data.mistat.india.xiaomi.com",
            "tracking.miui.com",
            "ad.hisense.com",
            "unified-ad.hisense.com",
            "ad.vidaahub.com",
            "ad.sony.tv",
            "flingo.tv",
            "tvinteractive.tv",
            "samba.tv",
            "ads.samba.tv",
            "acr.samba.tv",
            "alphonso.tv",
            "data.alphonso.tv",
            "inscape.tv",
            "ads.tv",
            "smarttv.ads"
        )

        val AD_NETWORK_DOMAINS: Set<String> = setOf(
            "doubleclick.net",
            "googlesyndication.com",
            "googleadservices.com",
            "admob.com",
            "app-measurement.com",
            "inmobi.com",
            "inmobicdn.net",
            "unityads.unity3d.com",
            "applovin.com",
            "applvn.com",
            "ironsrc.com",
            "supersonicads.com",
            "vungle.com",
            "chartboost.com",
            "mopub.com",
            "adcolony.com",
            "startappservice.com",
            "taboola.com",
            "outbrain.com",
            "criteo.com",
            "criteo.net",
            "pubmatic.com",
            "rubiconproject.com",
            "openx.net",
            "adnxs.com",
            "casalemedia.com",
            "indexww.com",
            "advertising.com",
            "amazon-adsystem.com",
            "media.net",
            "mgid.com",
            "revcontent.com",
            "popads.net",
            "popcash.net",
            "propellerads.com",
            "exoclick.com",
            "adsterra.com",
            "smartadserver.com",
            "yieldmo.com",
            "tapjoy.com",
            "fyber.com",
            "pangle-global.com",
            "an.facebook.com",
            "ads.facebook.com",
            "pixel.facebook.com",
            "ads.twitter.com",
            "ads.linkedin.com",
            "ads.tiktok.com",
            "ads.pinterest.com",
            "ads.yahoo.com",
            "adroll.com",
            "bidswitch.net",
            "lijit.com",
            "33across.com",
            "sharethrough.com",
            "triplelift.com",
            "adform.net",
            "teads.tv",
            "gumgum.com",
            "contextweb.com",
            "sonobi.com",
            "sovrn.com",
            "rhythmone.com",
            "spotxchange.com",
            "springserve.com",
            "lkqd.net",
            "colombiaonline.com",
            "adgebra.co.in",
            "vserv.mobi",
            "komli.com",
            "adnxs-simple.com",
            "adsrvr.org",
            "agkn.com",
            "mathtag.com",
            "mediamath.com",
            "turn.com",
            "zemanta.com",
            "zergnet.com"
        )

        val STREAMING_AD_DOMAINS: Set<String> = setOf(
            "fwmrm.net",
            "2mdn.net",
            "innovid.com",
            "freewheel.tv",
            "tremorhub.com",
            "spotx.tv",
            "brightroll.com",
            "yume.com",
            "adap.tv",
            "vidible.tv",
            "connatix.com",
            "jwpltx.com",
            "ads.stickyadstv.com",
            "ads.undertone.com",
            "ads.brightcove.com",
            "doubleverify.com",
            "moatads.com",
            "iasds01.com",
            "adsafeprotected.com",
            "scorecardresearch.com",
            "imrworldwide.com",
            "demdex.net",
            "omtrdc.net",
            "krxd.net",
            "bluekai.com",
            "rlcdn.com",
            "exelator.com",
            "eyeota.net",
            "lotame.com",
            "tapad.com",
            "liveramp.com",
            "ads.hotstar.com",
            "bifrost-api.hotstar.com",
            "ads.jiocinema.com",
            "ads.sonyliv.com",
            "ads.zee5.com",
            "ads.mxplayer.in",
            "adserver.mxplayer.in",
            "ads.pluto.tv",
            "siloh.pluto.tv",
            "ads.tubitv.com",
            "rainbarrel.tubitv.com"
        )

        val TRACKER_DOMAINS: Set<String> = setOf(
            "appsflyer.com",
            "adjust.com",
            "branch.io",
            "mixpanel.com",
            "amplitude.com",
            "segment.io",
            "segment.com",
            "flurry.com",
            "bugsnag.com",
            "newrelic.com",
            "hotjar.com",
            "fullstory.com",
            "mouseflow.com",
            "clarity.ms",
            "mc.yandex.ru",
            "stats.g.doubleclick.net",
            "analytics.google.com",
            "google-analytics.com",
            "ssl.google-analytics.com",
            "region1.google-analytics.com",
            "clevertap-prod.com",
            "wzrkt.com",
            "moengage.com",
            "onesignal.com",
            "leanplum.com",
            "kochava.com",
            "singular.net",
            "tenjin.io",
            "quantserve.com",
            "chartbeat.com",
            "parsely.com",
            "events.data.microsoft.com",
            "vortex.data.microsoft.com",
            "telemetry.mozilla.org",
            "metrics.icloud.com",
            "track.adform.net",
            "sync.outbrain.com",
            "pixel.rubiconproject.com"
        )
    }
}
