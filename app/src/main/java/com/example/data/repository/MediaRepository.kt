package com.example.data.repository

import android.util.Log
import com.example.data.api.ApiService
import com.example.data.model.ContentSection
import com.example.data.model.EpisodeItem
import com.example.data.model.LiveChannel
import com.example.data.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class MediaRepository {
    private val TAG = "MediaRepository"

    suspend fun getHomeContent(): Triple<MediaItem?, List<ContentSection>, List<LiveChannel>> = withContext(Dispatchers.IO) {
        // Ping services in background
        ApiService.pingServices()

        var heroItem: MediaItem? = null
        val sections = mutableListOf<ContentSection>()

        // 1. Fetch real Bioscope sections
        val bioscopeJson = ApiService.fetchBioscopePage()
        if (!bioscopeJson.isNullOrEmpty()) {
            try {
                val root = JSONObject(bioscopeJson)
                val result = root.optJSONObject("result")
                val jsonSections = result?.optJSONArray("sections")

                if (jsonSections != null) {
                    for (i in 0 until jsonSections.length()) {
                        val secObj = jsonSections.getJSONObject(i)
                        val title = secObj.optString("title", "Featured")
                        val size = secObj.optString("size")
                        val itemsArray = secObj.optJSONArray("items") ?: continue

                        val mediaList = mutableListOf<MediaItem>()
                        for (j in 0 until itemsArray.length()) {
                            val itemObj = itemsArray.getJSONObject(j)
                            val contentObj = itemObj.optJSONObject("content") ?: itemObj
                            val id = contentObj.optString("id", itemObj.optString("id", "item_$j"))
                            val itemTitle = contentObj.optString("title", itemObj.optString("title", "Untitled"))
                            val desc = contentObj.optString("description")
                            val type = contentObj.optString("type", "movies")
                            val poster = contentObj.optString("poster").ifEmpty {
                                contentObj.optString("tv_cover").ifEmpty {
                                    contentObj.optString("thumbnail")
                                }
                            }
                            val backdrop = contentObj.optString("poster_background").ifEmpty {
                                contentObj.optString("thumbnail_background").ifEmpty { poster }
                            }
                            val streamUrl = contentObj.optString("url")
                            val label = contentObj.optString("label", "Free")
                            val durationSec = contentObj.optInt("duration", 0)
                            val formattedDuration = if (durationSec > 0) {
                                val hrs = durationSec / 3600
                                val mins = (durationSec % 3600) / 60
                                if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
                            } else "2h 10m"

                            // Cast
                            val castList = mutableListOf<String>()
                            val metas = contentObj.optJSONObject("metas")
                            val castsArray = metas?.optJSONArray("casts")
                            if (castsArray != null) {
                                for (k in 0 until castsArray.length()) {
                                    val c = castsArray.getJSONObject(k).optString("title")
                                    if (c.isNotEmpty()) castList.add(c)
                                }
                            }

                            val mediaItem = MediaItem(
                                id = id,
                                title = itemTitle,
                                description = desc,
                                category = type,
                                posterUrl = poster,
                                backdropUrl = backdrop,
                                streamUrl = streamUrl,
                                rating = "4.6",
                                year = "2024",
                                duration = formattedDuration,
                                genre = if (type.contains("series")) "Drama Series" else "Action / Thriller",
                                label = label.ifEmpty { "HD" },
                                cast = castList
                            )

                            if (size == "hero_slider" && heroItem == null && streamUrl.isNotEmpty()) {
                                heroItem = mediaItem
                            }
                            mediaList.add(mediaItem)
                        }

                        if (mediaList.isNotEmpty() && size != "hero_slider") {
                            sections.add(ContentSection(title = title, items = mediaList))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing Bioscope JSON: ${e.message}")
            }
        }

        // Add Bollywood & Hindi Dubbed categories as requested
        val bollywoodItems = getCuratedBollywoodItems()
        val hindiDubbedItems = getCuratedHindiDubbedItems()
        val bongoSpecials = getCuratedBongoSpecials()

        sections.add(0, ContentSection(title = "Recommendations", items = bollywoodItems + hindiDubbedItems))
        sections.add(ContentSection(title = "Bollywood Blockbusters", items = bollywoodItems))
        sections.add(ContentSection(title = "Hindi Dubbed Hits", items = hindiDubbedItems))
        sections.add(ContentSection(title = "Bongo Specials & Shows", items = bongoSpecials))

        if (heroItem == null && bollywoodItems.isNotEmpty()) {
            heroItem = bollywoodItems[0]
        }

        // 2. Fetch Live IPTV Channels
        val liveChannels = ApiService.fetchIptvPlaylist().ifEmpty {
            getDefaultLiveChannels()
        }

        Triple(heroItem, sections, liveChannels)
    }

    suspend fun getLiveChannels(): List<LiveChannel> = withContext(Dispatchers.IO) {
        ApiService.fetchIptvPlaylist().ifEmpty {
            getDefaultLiveChannels()
        }
    }

    private fun getCuratedBollywoodItems(): List<MediaItem> {
        return listOf(
            MediaItem(
                id = "bolly_1",
                title = "The Batman",
                description = "Batman ventures into Gotham City's underworld when a sadistic killer leaves behind a trail of cryptic clues. As the evidence begins to lead closer to home, he must forge new relationships and unmask the culprit.",
                category = "bollywood",
                posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1200&auto=format&fit=crop&q=80",
                streamUrl = "https://vhx9nfhlsy.gpcdn.net/transcoded/2025/02/16/1032743/1/3/1835/manifest.m3u8",
                rating = "4.6",
                year = "2022",
                duration = "2h 56m",
                genre = "Fantasy • Action",
                label = "VIP",
                cast = listOf("Robert Pattinson", "Zoë Kravitz", "Paul Dano")
            ),
            MediaItem(
                id = "bolly_2",
                title = "Jawan",
                description = "A high-octane action thriller outlining the emotional journey of a man who is set to rectify the wrongs in the society, alongside a deeply personal vendetta.",
                category = "bollywood",
                posterUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=1200&auto=format&fit=crop&q=80",
                streamUrl = "https://4397879b.wurl.com/master/f36d25e7e52f1ba8d7e56eb859c636563214f541/UmFrdXRlblRWLWRlX0ZJRkFQbHVzR2VybWFuX0hMUw/playlist.m3u8",
                rating = "4.7",
                year = "2023",
                duration = "2h 49m",
                genre = "Action • Thriller",
                label = "Subscription",
                cast = listOf("Shah Rukh Khan", "Nayanthara", "Vijay Sethupathi")
            ),
            MediaItem(
                id = "bolly_3",
                title = "Pathaan",
                description = "An Indian spy takes on the leader of a group of mercenaries who have nefarious plans to target his homeland.",
                category = "bollywood",
                posterUrl = "https://images.unsplash.com/photo-1618336753974-aae8e04506aa?w=600&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1618336753974-aae8e04506aa?w=1200&auto=format&fit=crop&q=80",
                streamUrl = "https://d4ddgdmj1cvnm.cloudfront.net/scheduler/scheduleMaster/409.m3u8",
                rating = "4.5",
                year = "2023",
                duration = "2h 26m",
                genre = "Spy • Action",
                label = "Subscription",
                cast = listOf("Shah Rukh Khan", "Deepika Padukone", "John Abraham")
            ),
            MediaItem(
                id = "bolly_4",
                title = "Animal",
                description = "The hardened son of a powerful industrialist returns home after years abroad and vows ruthless vengeance when an assassination attempt is made on his father.",
                category = "bollywood",
                posterUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=600&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=1200&auto=format&fit=crop&q=80",
                streamUrl = "https://xumo-xumoent-vc-122-sjv70.fast.nbcuni.com/live/master.m3u8",
                rating = "4.4",
                year = "2023",
                duration = "3h 24m",
                genre = "Crime • Drama",
                label = "VIP",
                cast = listOf("Ranbir Kapoor", "Anil Kapoor", "Rashmika Mandanna")
            )
        )
    }

    private fun getCuratedHindiDubbedItems(): List<MediaItem> {
        return listOf(
            MediaItem(
                id = "hd_1",
                title = "Doctor Strange: Multiverse of Madness",
                description = "Doctor Strange teams up with a mysterious teenage girl from his dreams who can travel across multiverses, to battle multiple threats, including other-universe versions of himself.",
                category = "hindi-dubbed",
                posterUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=600&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=1200&auto=format&fit=crop&q=80",
                streamUrl = "https://vhx9nfhlsy.gpcdn.net/transcoded/2025/02/16/1032743/1/3/1835/manifest.m3u8",
                rating = "4.6",
                year = "2022",
                duration = "2h 06m",
                genre = "Sci-Fi • Fantasy",
                label = "Subscription"
            ),
            MediaItem(
                id = "hd_2",
                title = "Avengers: Infinity War",
                description = "The Avengers and their allies must be willing to sacrifice all in an attempt to defeat the powerful Thanos before his blitz of devastation and ruin puts an end to the universe.",
                category = "hindi-dubbed",
                posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop&q=80",
                streamUrl = "https://4397879b.wurl.com/master/f36d25e7e52f1ba8d7e56eb859c636563214f541/UmFrdXRlblRWLWRlX0ZJRkFQbHVzR2VybWFuX0hMUw/playlist.m3u8",
                rating = "4.8",
                year = "2018",
                duration = "2h 29m",
                genre = "Superhero • Action",
                label = "Subscription"
            ),
            MediaItem(
                id = "hd_3",
                title = "Spider-Man: No Way Home",
                description = "With Spider-Man's identity now revealed, Peter asks Doctor Strange for help. When a spell goes wrong, dangerous foes from other worlds start to appear.",
                category = "hindi-dubbed",
                posterUrl = "https://images.unsplash.com/photo-1635805737707-575885ab0820?w=600&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1635805737707-575885ab0820?w=1200&auto=format&fit=crop&q=80",
                streamUrl = "https://d4ddgdmj1cvnm.cloudfront.net/scheduler/scheduleMaster/409.m3u8",
                rating = "4.7",
                year = "2021",
                duration = "2h 28m",
                genre = "Adventure • Sci-Fi",
                label = "Subscription"
            ),
            MediaItem(
                id = "hd_4",
                title = "Dune: Part Two",
                description = "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.",
                category = "hindi-dubbed",
                posterUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=600&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=1200&auto=format&fit=crop&q=80",
                streamUrl = "https://xumo-xumoent-vc-122-sjv70.fast.nbcuni.com/live/master.m3u8",
                rating = "4.8",
                year = "2024",
                duration = "2h 46m",
                genre = "Epic • Sci-Fi",
                label = "VIP"
            )
        )
    }

    private fun getCuratedBongoSpecials(): List<MediaItem> {
        val episodes = listOf(
            EpisodeItem("ep1", "Episode 1: The Beginning", 1, "42m", "", "https://vhx9nfhlsy.gpcdn.net/transcoded/2025/02/16/1032743/1/3/1835/manifest.m3u8"),
            EpisodeItem("ep2", "Episode 2: The Conspiracy", 2, "44m", "", "https://4397879b.wurl.com/master/f36d25e7e52f1ba8d7e56eb859c636563214f541/UmFrdXRlblRWLWRlX0ZJRkFQbHVzR2VybWFuX0hMUw/playlist.m3u8"),
            EpisodeItem("ep3", "Episode 3: The Crossroad", 3, "46m", "", "https://d4ddgdmj1cvnm.cloudfront.net/scheduler/scheduleMaster/409.m3u8"),
            EpisodeItem("ep4", "Episode 4: The Final Truth", 4, "50m", "", "https://xumo-xumoent-vc-122-sjv70.fast.nbcuni.com/live/master.m3u8")
        )

        return listOf(
            MediaItem(
                id = "Yz0CXXKe2IT",
                title = "Hotel Relax - Bongo Special",
                description = "A renowned business mogul checks into a luxury hotel unaware of the bizarre syndicate operating beneath its glamorous surface. Full suspense drama series.",
                category = "bongo",
                posterUrl = "https://images.unsplash.com/photo-1542204165-65bf26472b9b?w=600&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1542204165-65bf26472b9b?w=1200&auto=format&fit=crop&q=80",
                streamUrl = "https://vhx9nfhlsy.gpcdn.net/transcoded/2025/02/16/1032743/1/3/1835/manifest.m3u8",
                rating = "4.7",
                year = "2024",
                duration = "4 Episodes",
                genre = "Comedy • Mystery",
                label = "Exclusive",
                episodes = episodes
            ),
            MediaItem(
                id = "bongo_meta_2",
                title = "Ami Ki Tumi",
                description = "A mysterious parallel dimension collision forces an ordinary young man to question reality and identity across two worlds.",
                category = "bongo",
                posterUrl = "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=600&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=1200&auto=format&fit=crop&q=80",
                streamUrl = "https://4397879b.wurl.com/master/f36d25e7e52f1ba8d7e56eb859c636563214f541/UmFrdXRlblRWLWRlX0ZJRkFQbHVzR2VybWFuX0hMUw/playlist.m3u8",
                rating = "4.6",
                year = "2023",
                duration = "6 Episodes",
                genre = "Sci-Fi • Thriller",
                label = "Bongo Original",
                episodes = episodes
            )
        )
    }

    private fun getDefaultLiveChannels(): List<LiveChannel> {
        return listOf(
            LiveChannel(
                id = "ch_fifa",
                name = "FIFA+ Live",
                logoUrl = "https://s3.aynaott.com/storage/dc25a8382341855cd9847537971d869c",
                group = "Sports",
                streamUrl = "https://4397879b.wurl.com/master/f36d25e7e52f1ba8d7e56eb859c636563214f541/UmFrdXRlblRWLWRlX0ZJRkFQbHVzR2VybWFuX0hMUw/playlist.m3u8"
            ),
            LiveChannel(
                id = "ch_sports_first",
                name = "Sports First TV",
                logoUrl = "https://s3.aynaott.com/storage/748d28752dcf95740561f1ac39e15fc3",
                group = "Sports",
                streamUrl = "https://d4ddgdmj1cvnm.cloudfront.net/scheduler/scheduleMaster/409.m3u8"
            ),
            LiveChannel(
                id = "ch_nbc_sports",
                name = "NBC Sports",
                logoUrl = "https://s3.aynaott.com/storage/0a241a80bf51d2c3b3722531706ce086",
                group = "Sports",
                streamUrl = "https://xumo-xumoent-vc-122-sjv70.fast.nbcuni.com/live/master.m3u8"
            ),
            LiveChannel(
                id = "ch_unite8_1",
                name = "Unite8 Sports 1",
                logoUrl = "https://s3.aynaott.com/storage/4380e37cf05dc85035434fe0e395974d",
                group = "Sports",
                streamUrl = "https://4397879b.wurl.com/master/f36d25e7e52f1ba8d7e56eb859c636563214f541/UmFrdXRlblRWLWRlX0ZJRkFQbHVzR2VybWFuX0hMUw/playlist.m3u8"
            ),
            LiveChannel(
                id = "ch_bein1",
                name = "Bein Sports 1",
                logoUrl = "https://s3.aynaott.com/storage/3f60ee0c945319f264361f201a52ed49",
                group = "Sports",
                streamUrl = "https://d4ddgdmj1cvnm.cloudfront.net/scheduler/scheduleMaster/409.m3u8"
            ),
            LiveChannel(
                id = "ch_willow",
                name = "Willow TV",
                logoUrl = "https://s3.aynaott.com/storage/94a778ec3219f7eb54bdf1ee07a95788",
                group = "Sports",
                streamUrl = "https://xumo-xumoent-vc-122-sjv70.fast.nbcuni.com/live/master.m3u8"
            )
        )
    }
}
