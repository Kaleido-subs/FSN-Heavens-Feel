import myaa.subkt.ass.*
import myaa.subkt.tasks.*
import myaa.subkt.tasks.Mux.*
import myaa.subkt.tasks.Nyaa.*
import java.awt.Color
import java.time.*

plugins {
    id("myaa.subkt")
}

fun String.isKaraTemplate(): Boolean {
	return this.startsWith("code") || this.startsWith("template") || this.startsWith("mixin")
}

fun EventLine.isKaraTemplate(): Boolean {
	return this.comment && this.effect.isKaraTemplate()
}


subs {
    readProperties("sub.properties")
    episodes(getList("episodes"))

    val dialogue_ktemplate by task<Automation> {
        from(get("dialogue"))

        video(get("premux"))
		script("0x.KaraTemplater.moon")
		macro("0x539's Templater")
		loglevel(Automation.LogLevel.WARNING)
	}

    val ed_ktemplate by task<Automation> {
        if (file(get("ED")).exists()) {
            from(get("ED"))
        }

        video(get("premux"))
		script("0x.KaraTemplater.moon")
		macro("0x539's Templater")
		loglevel(Automation.LogLevel.WARNING)
	}

    val foreign_ktemplate by task<Automation> {
        from(get("foreign"))

        video(get("premux"))
		script("0x.KaraTemplater.moon")
		macro("0x539's Templater")
		loglevel(Automation.LogLevel.WARNING)
	}


    merge {
        from(dialogue_ktemplate.item())

        from(ed_ktemplate.item())

        if (file(get("IS")).exists()) {
            from(get("IS"))
        }

        from(get("TS"))

    }

	val cleanmerge_full by task<ASS> {
		from(merge.item())
    	ass {
			events.lines.removeIf { it.isKaraTemplate() }
	    }

        out(get("mergedname_full"))
	}

    val merge_ss by task<Merge> {
		if (file(get("foreign")).exists()) {
			from(foreign_ktemplate.item())
		}

        from(ed_ktemplate.item())

        if (file(get("IS")).exists()) {
            from(get("IS"))
        }

        from(get("TS"))
    }

	val cleanmerge_ss by task<ASS> {
		from(merge_ss.item())
    	ass {
			events.lines.removeIf { it.isKaraTemplate() }
	    }

        out(get("mergedname_ss"))
	}

	// "virtual" task to easily run both cleanmerges
	val cleanmerge by task<DefaultSubTask> {
		dependsOn(cleanmerge_ss.item(), cleanmerge_full.item())
	}

    mux {
        title(get("filetitle"))

        skipUnusedFonts(true)

		from(get("premux")) {
			video {
				lang("jpn")
				default(true)
			}
            if (tracks.count { it.track.type == TrackType.AUDIO } == 1) {
                audio(0) {
                    lang("jpn")
                    default(true)
                    forced(true)
                }
            } else {
                audio(0) {
                    lang("jpn")
                    default(true)
                    forced(true)
                }
                audio(1) {
                    lang("eng")
                    default(false)
                    forced(false)
                }
            }
            subtitles {
                include(false)
            }
            includeChapters(true)
			attachments { include(false) }
		}

        from(cleanmerge_ss.item()) {
            tracks {
                name(get("subtitle_full"))
                lang("eng")
                default(true)
				forced(true)
                compression(CompressionType.ZLIB)
            }
        }

		from(cleanmerge_full.item()) {
			tracks {
                name(get("subtitle_ss"))
				lang("eng")
				default(false)
				forced(false)
				compression(CompressionType.ZLIB)
			}
		}

        attach(get("common_fonts")) {
            includeExtensions("ttf", "otf")
        }

        attach(get("fonts")) {
            includeExtensions("ttf", "otf")
        }

        out(get("muxout"))
    }
}
