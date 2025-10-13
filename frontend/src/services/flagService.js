// Create flagService.js
import ApiService from "./api";

class FlagService {
  constructor() {
    this.flagCache = new Map();
    this.countriesLoaded = false;
  }

  // Load all countries and their flags once
  async loadCountries() {
    if (this.countriesLoaded) return;

    try {
      const countries = await ApiService.getAllCountries();
      countries.forEach((country) => {
        if (country.name && country.flagUrl) {
          this.flagCache.set(country.name.toLowerCase(), country.flagUrl);
        }
      });
      this.countriesLoaded = true;
      console.log(`Loaded ${this.flagCache.size} country flags`);
    } catch (error) {
      console.error("Error loading countries:", error);
    }
  }

  // Get flag URL for a country name
  getFlagUrl(countryName) {
    if (!countryName) return null;
    return this.flagCache.get(countryName.toLowerCase()) || null;
  }

  // Fallback emoji flags (as backup)
  getEmojiFlag(countryName) {
    const flagMap = {
      england: "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
      spain: "🇪🇸",
      france: "🇫🇷",
      germany: "🇩🇪",
      brazil: "🇧🇷",
      argentina: "🇦🇷",
      portugal: "🇵🇹",
      italy: "🇮🇹",
      netherlands: "🇳🇱",
      belgium: "🇧🇪",
      croatia: "🇭🇷",
      uruguay: "🇺🇾",
      colombia: "🇨🇴",
      mexico: "🇲🇽",
      poland: "🇵🇱",
      denmark: "🇩🇰",
      sweden: "🇸🇪",
      norway: "🇳🇴",
      switzerland: "🇨🇭",
      austria: "🇦🇹",
      "czech republic": "🇨🇿",
      morocco: "🇲🇦",
      senegal: "🇸🇳",
      nigeria: "🇳🇬",
      ghana: "🇬🇭",
      egypt: "🇪🇬",
      algeria: "🇩🇿",
      tunisia: "🇹🇳",
      cameroon: "🇨🇲",
      "ivory coast": "🇨🇮",
      "south africa": "🇿🇦",
      japan: "🇯🇵",
      "south korea": "🇰🇷",
      australia: "🇦🇺",
      iran: "🇮🇷",
      "saudi arabia": "🇸🇦",
      qatar: "🇶🇦",
      "united states": "🇺🇸",
      canada: "🇨🇦",
      "costa rica": "🇨🇷",
      panama: "🇵🇦",
      jamaica: "🇯🇲",
      ecuador: "🇪🇨",
      peru: "🇵🇪",
      chile: "🇨🇱",
      paraguay: "🇵🇾",
      bolivia: "🇧🇴",
      venezuela: "🇻🇪",
      russia: "🇷🇺",
      ukraine: "🇺🇦",
      serbia: "🇷🇸",
      slovenia: "🇸🇮",
      slovakia: "🇸🇰",
      hungary: "🇭🇺",
      romania: "🇷🇴",
      bulgaria: "🇧🇬",
      greece: "🇬🇷",
      turkey: "🇹🇷",
      finland: "🇫🇮",
      iceland: "🇮🇸",
      ireland: "🇮🇪",
      scotland: "🏴󠁧󠁢󠁳󠁣󠁴󠁿",
      wales: "🏴󠁧󠁢󠁷󠁬󠁳󠁿",
      "northern ireland": "🇬🇧",
    };

    return flagMap[countryName.toLowerCase()] || "🌍";
  }

  // Get either flag URL or emoji as fallback
  getFlag(countryName) {
    const flagUrl = this.getFlagUrl(countryName);
    if (flagUrl) {
      return { type: "url", value: flagUrl };
    }
    return { type: "emoji", value: this.getEmojiFlag(countryName) };
  }
}

export default new FlagService();
