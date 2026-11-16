package com.example.data.model

data class SpaTreatment(
  val id: String,
  val title: String,
  val subtitle: String,
  val category: String,
  val durationMinutes: Int,
  val priceUsd: Int,
  val rating: Double,
  val reviewCount: Int,
  val imageUrl: String,
  val description: String,
  val botanicalNotes: List<String>,
  val highlights: List<String>,
  val isVipSignature: Boolean = false,
  val isPackageBundle: Boolean = false,
)

data class SpaTherapist(
  val id: String,
  val name: String,
  val title: String,
  val specialty: String,
  val yearsExperience: Int,
  val rating: Double,
  val bio: String,
  val avatarUrl: String,
  val availableCategories: List<String>,
)

data class RitualAddOn(
  val id: String,
  val name: String,
  val durationExtraMinutes: Int,
  val priceUsd: Int,
  val description: String,
)

object SpaCatalog {
  val categories = listOf("All", "Facials", "Massages", "Body Rituals", "Packages")

  val timeSlots =
    listOf("9:30 AM", "11:00 AM", "12:45 PM", "2:30 PM", "4:15 PM", "6:00 PM")

  val pressureLevels = listOf("Gentle", "Medium", "Firm", "Deep")

  val addOns =
    listOf(
      RitualAddOn(
        id = "gua_sha",
        name = "Jade Gua Sha Sculpt",
        durationExtraMinutes = 15,
        priceUsd = 35,
        description = "Lymphatic facial contouring with chilled nephrite jade stones.",
      ),
      RitualAddOn(
        id = "basalt_stones",
        name = "Warm Basalt Stones",
        durationExtraMinutes = 15,
        priceUsd = 40,
        description = "Mineral-rich volcanic stones placed along meridian tension points.",
      ),
      RitualAddOn(
        id = "neroli_scalp",
        name = "Neroli Scalp Elixir",
        durationExtraMinutes = 15,
        priceUsd = 30,
        description = "Warm organic orange-blossom oil acupressure scalp ritual.",
      ),
      RitualAddOn(
        id = "salt_soak",
        name = "Himalayan Foot Soak",
        durationExtraMinutes = 10,
        priceUsd = 25,
        description = "Rose-petal and pink mineral bath prior to your table session.",
      ),
    )

  val treatments =
    listOf(
      SpaTreatment(
        id = "sage_botanical_facial",
        title = "Sage Botanical Renewal Facial",
        subtitle = "Cellular radiance & sculpted lymph drainage",
        category = "Facials",
        durationMinutes = 75,
        priceUsd = 210,
        rating = 4.9,
        reviewCount = 148,
        imageUrl =
          "https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?auto=format&fit=crop&w=1000&q=80",
        description =
          "A deeply restorative facial combining wild-harvested white sage hydrosol, cryo-sculpting rose quartz, and bioactive peptide serums to calm redness and restore luminous bounce.",
        botanicalNotes = listOf("White Sage", "Chamomile", "Rosehip Oil"),
        highlights =
          listOf(
            "Double botanical steam cleanse",
            "Sculptural buccal & Gua Sha massage",
            "LED cellular light infusion",
          ),
        isVipSignature = true,
      ),
      SpaTreatment(
        id = "warm_stone_serenity_massage",
        title = "Warm Stone Serenity Massage",
        subtitle = "Slow-melt myofascial release",
        category = "Massages",
        durationMinutes = 90,
        priceUsd = 235,
        rating = 4.9,
        reviewCount = 214,
        imageUrl =
          "https://images.unsplash.com/photo-1544161515-4ab6ce6db874?auto=format&fit=crop&w=1000&q=80",
        description =
          "Heated river basalt stones glide in long, rhythmic strokes with warm cedarwood and bergamot oil, unwinding deep muscular knots and quieting the nervous system.",
        botanicalNotes = listOf("Cedarwood", "Bergamot", "Sweet Almond"),
        highlights =
          listOf(
            "Heated basalt meridian placement",
            "Full-body slow Swedish & myofascial work",
            "Guided breathwork opening",
          ),
        isVipSignature = false,
      ),
      SpaTreatment(
        id = "golden_hammam_ritual",
        title = "Golden Hammam Silk Ritual",
        subtitle = "Moroccan black soap & honey body polish",
        category = "Body Rituals",
        durationMinutes = 90,
        priceUsd = 265,
        rating = 5.0,
        reviewCount = 96,
        imageUrl =
          "https://images.unsplash.com/photo-1540555700478-4be289fbecef?auto=format&fit=crop&w=1000&q=80",
        description =
          "Performed on heated marble, this royal purification ritual pairs eucalyptus savon noir exfoliation with a warm rhassoul clay wrap and 24k gold-infused argan oil veil.",
        botanicalNotes = listOf("Argan Oil", "Eucalyptus", "Orange Blossom"),
        highlights =
          listOf(
            "Eucalyptus steam chamber warm-up",
            "Kessa silk exfoliation",
            "Gold-flecked argan hydration veil",
          ),
        isVipSignature = true,
      ),
      SpaTreatment(
        id = "oasis_sanctuary_half_day",
        title = "The Oasis Sanctuary Half-Day",
        subtitle = "Massage, renewal facial & private tea suite",
        category = "Packages",
        durationMinutes = 180,
        priceUsd = 460,
        rating = 5.0,
        reviewCount = 67,
        imageUrl =
          "https://images.unsplash.com/photo-1600334089648-b0d9d3028eb2?auto=format&fit=crop&w=1000&q=80",
        description =
          "Our signature immersion package: a 75-minute botanical facial, 75-minute aromatherapy massage, private mineral soaking tub, and organic matcha pastry service in the courtyard.",
        botanicalNotes = listOf("Matcha", "Hinoki Cypress", "Neroli"),
        highlights =
          listOf(
            "Private cedar soaking suite (30 min)",
            "Two full-length custom treatments",
            "Curated herbal tea & tonic pairing",
          ),
        isVipSignature = true,
        isPackageBundle = true,
      ),
      SpaTreatment(
        id = "deep_hinoki_aromatherapy",
        title = "Hinoki Forest Aromatherapy",
        subtitle = "Restorative lymphatic & acupressure flow",
        category = "Massages",
        durationMinutes = 60,
        priceUsd = 175,
        rating = 4.8,
        reviewCount = 132,
        imageUrl =
          "https://images.unsplash.com/photo-1519823551278-64ac92734fb1?auto=format&fit=crop&w=1000&q=80",
        description =
          "Inspired by Japanese forest bathing, warm cypress and vetiver oils restore circulation, soften neck tension, and promote deeply restful sleep.",
        botanicalNotes = listOf("Hinoki", "Vetiver", "Yuzu"),
        highlights =
          listOf(
            "Inhalation grounding ceremony",
            "Targeted upper back & neck release",
            "Warm herbal compress finish",
          ),
      ),
      SpaTreatment(
        id = "luminous_gold_collagen_facial",
        title = "Luminous Gold Collagen Lift",
        subtitle = "Microcurrent toning & velvet hydration",
        category = "Facials",
        durationMinutes = 60,
        priceUsd = 195,
        rating = 4.9,
        reviewCount = 89,
        imageUrl =
          "https://images.unsplash.com/photo-1512290923902-8a9f81dc236c?auto=format&fit=crop&w=1000&q=80",
        description =
          "Gentle bio-electric microcurrents lift facial contours while botanical hyaluronic acid and silk amino masks leave skin supple and radiant.",
        botanicalNotes = listOf("Silk Amino", "Saffron", "Camellia"),
        highlights =
          listOf(
            "Gentle enzymatic glow polish",
            "Microcurrent contour toning",
            "Collagen hydro-jelly veil",
          ),
        isVipSignature = true,
      ),
      SpaTreatment(
        id = "twilight_tranquility_duo",
        title = "Twilight Tranquility Package",
        subtitle = "Body scrub, scalp ritual & sound bath",
        category = "Packages",
        durationMinutes = 135,
        priceUsd = 340,
        rating = 4.9,
        reviewCount = 54,
        imageUrl =
          "https://images.unsplash.com/photo-1515377905703-c4788e51af15?auto=format&fit=crop&w=1000&q=80",
        description =
          "Designed for evening decompression: sea-kelp mineral exfoliation, slow acupressure massage, and a private crystal singing-bowl resonance closing.",
        botanicalNotes = listOf("Lavender", "Sea Kelp", "Frankincense"),
        highlights =
          listOf(
            "Mineral-rich sea kelp polish",
            "Warm lavender oil massage",
            "Crystal bowl sound immersion",
          ),
        isPackageBundle = true,
      ),
    )

  val therapists =
    listOf(
      SpaTherapist(
        id = "claire_moreau",
        name = "Claire Moreau",
        title = "Lead Holistic Esthetician",
        specialty = "Sculptural Facials & Lymphatics",
        yearsExperience = 11,
        rating = 4.98,
        bio = "Trained in Paris and Kyoto, Claire blends sculptural buccal techniques with organic botanical actives.",
        avatarUrl =
          "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=500&q=80",
        availableCategories = listOf("Facials", "Packages"),
      ),
      SpaTherapist(
        id = "nadia_khoury",
        name = "Nadia Khoury",
        title = "Master Bodywork Specialist",
        specialty = "Hammam & Warm Stone Therapy",
        yearsExperience = 9,
        rating = 4.96,
        bio = "Specializing in traditional North African hammam rituals and deep myofascial restoration.",
        avatarUrl =
          "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?auto=format&fit=crop&w=500&q=80",
        availableCategories = listOf("Massages", "Body Rituals", "Packages"),
      ),
      SpaTherapist(
        id = "sora_takahashi",
        name = "Sora Takahashi",
        title = "Senior Shiatsu & Aroma Therapist",
        specialty = "Hinoki Shiatsu & Sound Healing",
        yearsExperience = 13,
        rating = 4.99,
        bio = "Brings calm rhythmic meridian work and sensory Japanese forest aromatherapy to every session.",
        avatarUrl =
          "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=500&q=80",
        availableCategories = listOf("Massages", "Body Rituals", "Facials", "Packages"),
      ),
    )
}
