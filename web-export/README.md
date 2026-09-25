# SAYA Voice Assistant (React + TypeScript)

SAYA is a production-grade, Siri-like voice assistant interface with a dark-mode minimalist aesthetic inspired by Apple Intelligence.

## Key Features

1. **Center Animated Voice Orb**:
   - **Idle**: Slow, subtle breathing pulse
   - **Listening**: Rapid pulse with concentric audio wave rings reacting in real-time to microphone RMS input
   - **Speaking**: Vibrant magenta / purple iridescent glow
   - **Thinking**: Rotating fluid aurora gradient

2. **Full Dark Mode Aesthetic**:
   - Deep obsidian space palette (`#08090e`, `#11141e`)
   - Apple dark iMessage styled chat bubbles
   - Smooth 60fps animations and fluid transitions

3. **Voice Input/Output**:
   - Speech Recognition via Web Speech API
   - Text to Speech via Web SpeechSynthesis API
   - Real-time live transcript bubble while speaking

4. **Continuous Wake-Word Simulation**:
   - Listens for "Hey SAYA" in continuous foreground cycles with auto-restart on timeout.

5. **Command Routing**:
   - `open whatsapp` -> Opens WhatsApp Web (`https://web.whatsapp.com`)
   - `open youtube` -> Opens YouTube (`https://youtube.com`)
   - `open email` / `open gmail` -> Opens Gmail (`https://mail.google.com`)
   - `call [name]` -> Explains browser telephony limitation and triggers call handler
   - `send message to [name]` -> Directs to WhatsApp Web (`https://wa.me/`)
   - `what is the time` -> Speaks and displays current local time
   - `weather today` -> Calls Open-Meteo REST API (no API key required)
   - `search [query] on google` -> Launches Google Search
   - **Conversational Fallback**: Calls Google Gemini API (`@google/genai`) with "friendly neighbor" persona SAYA

6. **College Presentation Architecture Guide**:
   - Includes full architectural comparison between Web Speech API and native Android Background Services (Porcupine/Vosk wake-word DSP, Android Intents, ContactsContract, ACTION_DIAL).

## Running the Web Version

```bash
cd web-export
npm install
npm run dev
```
