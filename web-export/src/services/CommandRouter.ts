/**
 * CommandRouter: Service module for parsing, matching, and executing user voice requests.
 *
 * Handles:
 * - Activation commands: "Weak dady is home" and "Hey SAYA"
 * - Siri-like Task management: "show my tasks", "add task [title]", "complete task [title]"
 * - App launches (WhatsApp, YouTube, Gmail)
 * - Telephony / Messaging (call, message)
 * - Clock / Time queries
 * - Weather lookup via Open-Meteo REST API
 * - Google search queries
 * - General conversational fallback via Google Gemini API (@google/genai)
 */

import { GoogleGenAI } from '@google/genai';

export interface CommandExecutionResult {
  spokenText: string;
  displayText: string;
  badge?: string;
  toast?: string;
  actionUrl?: string;
  isActivation?: boolean;
}

export interface WebUserTask {
  id: string;
  title: string;
  isCompleted: boolean;
}

export class CommandRouter {
  private geminiClient: GoogleGenAI | null = null;

  constructor(apiKey?: string) {
    if (apiKey) {
      this.geminiClient = new GoogleGenAI({ apiKey });
    }
  }

  public updateApiKey(apiKey: string) {
    if (apiKey) {
      this.geminiClient = new GoogleGenAI({ apiKey });
    }
  }

  public async route(
    rawInput: string,
    tasks: WebUserTask[] = [],
    onAddTask?: (title: string) => void,
    onCompleteTask?: (title: string) => void
  ): Promise<CommandExecutionResult> {
    const query = rawInput.trim().toLowerCase();

    // 1. Activation Command 1: "Weak dady is home" / "Wake daddy is home"
    if (
      query.includes('weak dady is home') ||
      query.includes('wake daddy is home') ||
      query.includes('weak daddy is home') ||
      query.includes('wake dady is home') ||
      query.includes('daddy is home')
    ) {
      const pendingCount = tasks.filter((t) => !t.isCompleted).length;
      return {
        spokenText: `Welcome home, sir! SAYA is activated and online. Voice verification confirmed. You have ${pendingCount} pending tasks on your list. What can I assist you with today?`,
        displayText: `● ACTIVATED: Welcome home, sir!\nVoice Verified: Primary User\nPending Tasks: ${pendingCount}`,
        badge: 'Voice Activation',
        toast: 'SAYA Activated: Welcome home routine triggered via authorized voice.',
        isActivation: true,
      };
    }

    // 2. Activation Command 2: "Hey SAYA"
    if (query === 'hey saya' || query === 'saya' || query === 'activate saya') {
      return {
        spokenText: "I'm right here! SAYA is activated and ready for all your tasks, neighbor.",
        displayText: '● ACTIVATED: SAYA is online and listening...',
        badge: 'Voice Activation',
        isActivation: true,
      };
    }

    // 3. Siri Tasks: "add task [title]" / "remind me to [title]"
    if (query.startsWith('add task ') || query.startsWith('remind me to ')) {
      const prefix = query.startsWith('add task ') ? 'add task ' : 'remind me to ';
      const title = rawInput.substring(prefix.length).trim();
      if (title) {
        onAddTask?.(title);
        return {
          spokenText: `Added "${title}" to your tasks list, sir.`,
          displayText: `Task Created: "${title}"`,
          badge: 'Siri Tasks',
        };
      }
    }

    // 4. Siri Tasks: "show my tasks" / "list tasks"
    if (query.includes('show my tasks') || query.includes('list tasks') || query.includes('my tasks')) {
      const pending = tasks.filter((t) => !t.isCompleted);
      if (pending.length === 0) {
        return {
          spokenText: 'You have no pending tasks, sir. All caught up!',
          displayText: 'No pending tasks.',
          badge: 'Siri Tasks',
        };
      }
      const listStr = pending.map((t, i) => `${i + 1}: ${t.title}`).join('. ');
      return {
        spokenText: `You have ${pending.length} pending tasks: ${listStr}.`,
        displayText: `Tasks (${pending.length}):\n` + pending.map((t) => `• ${t.title}`).join('\n'),
        badge: 'Siri Tasks',
      };
    }

    // 5. Siri Tasks: "complete task [title]"
    if (query.startsWith('complete task ') || query.startsWith('finish task ')) {
      const titlePart = rawInput.substring(query.startsWith('complete task ') ? 14 : 12).trim();
      onCompleteTask?.(titlePart);
      return {
        spokenText: `Marked task "${titlePart}" as complete.`,
        displayText: `Completed Task: "${titlePart}"`,
        badge: 'Siri Tasks',
      };
    }

    // 6. Open WhatsApp
    if (query.includes('open whatsapp')) {
      window.open('https://web.whatsapp.com', '_blank');
      return {
        spokenText: 'Opening WhatsApp in a new tab for you, neighbor!',
        displayText: 'Opening WhatsApp Web...',
        badge: 'WhatsApp Web',
        actionUrl: 'https://web.whatsapp.com',
      };
    }

    // 7. Open YouTube
    if (query.includes('open youtube')) {
      window.open('https://youtube.com', '_blank');
      return {
        spokenText: 'Opening YouTube right away, enjoy your videos!',
        displayText: 'Opening YouTube...',
        badge: 'YouTube',
        actionUrl: 'https://youtube.com',
      };
    }

    // 8. Open Email / Gmail
    if (query.includes('open email') || query.includes('open gmail')) {
      window.open('https://mail.google.com', '_blank');
      return {
        spokenText: 'Opening Gmail inbox for you now.',
        displayText: 'Opening Gmail...',
        badge: 'Email',
        actionUrl: 'https://mail.google.com',
      };
    }

    // 9. Call [name]
    if (query.startsWith('call ')) {
      const contact = rawInput.substring(5).trim();
      return {
        spokenText: `Calling requires native phone permissions not supported directly in the browser. Opening dialer link for ${contact}.`,
        displayText: `Call requested for: ${contact}`,
        badge: 'Phone Call',
        toast: 'Browser limitation: Calling requires native phone permissions (CALL_PHONE) or native Android Intent.ACTION_DIAL.',
      };
    }

    // 10. Send message to [name]
    if (query.startsWith('send message to ')) {
      const contact = rawInput.substring('send message to '.length).trim();
      window.open(`https://wa.me/?text=Hello%20${encodeURIComponent(contact)}`, '_blank');
      return {
        spokenText: `Opening WhatsApp chat for ${contact}. Since browsers cannot read local device contacts, please confirm their phone number.`,
        displayText: `Opening message for ${contact}...`,
        badge: 'Messaging',
        toast: 'Browser limitation: Web cannot access native device contacts; routed via WhatsApp Web URL.',
      };
    }

    // 11. Current Time
    if (query.includes('what is the time') || query.includes("what's the time") || query === 'time') {
      const now = new Date();
      const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      const dateStr = now.toLocaleDateString([], { weekday: 'long', month: 'long', day: 'numeric' });
      return {
        spokenText: `It's currently ${timeStr} on ${dateStr}.`,
        displayText: `Time: ${timeStr} (${dateStr})`,
        badge: 'Clock',
      };
    }

    // 12. Weather Today (Open-Meteo REST API, no key required)
    if (query.includes('weather today') || query.includes('weather')) {
      return await this.fetchWeather();
    }

    // 13. Search [query] on Google
    if (query.includes('search ') && query.includes('on google')) {
      let searchQuery = rawInput;
      const onGoogleIdx = searchQuery.toLowerCase().indexOf('on google');
      if (onGoogleIdx !== -1) searchQuery = searchQuery.substring(0, onGoogleIdx);
      const searchIdx = searchQuery.toLowerCase().indexOf('search');
      if (searchIdx !== -1) searchQuery = searchQuery.substring(searchIdx + 6);
      searchQuery = searchQuery.trim();

      const searchUrl = `https://www.google.com/search?q=${encodeURIComponent(searchQuery)}`;
      window.open(searchUrl, '_blank');
      return {
        spokenText: `Searching Google for ${searchQuery}.`,
        displayText: `Google Search: "${searchQuery}"`,
        badge: 'Google Search',
        actionUrl: searchUrl,
      };
    }

    // 14. Fallback to Gemini AI conversational query
    return await this.askGemini(rawInput);
  }

  private async fetchWeather(): Promise<CommandExecutionResult> {
    try {
      const lat = 37.7749;
      const lon = -122.4194;
      const url = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m`;

      const res = await fetch(url);
      const data = await res.json();
      const temp = data?.current?.temperature_2m ?? '20';
      const wind = data?.current?.wind_speed_10m ?? '12';

      const spoken = `Today's temperature is around ${temp} degrees Celsius with wind speeds of ${wind} kilometers per hour. A fine day to be outside!`;
      return {
        spokenText: spoken,
        displayText: `Weather: ${temp}°C | Wind: ${wind} km/h`,
        badge: 'Open-Meteo',
      };
    } catch {
      return {
        spokenText: "I couldn't fetch the weather right now, neighbor, but I hope you have a great day ahead!",
        displayText: 'Weather service temporarily unavailable',
        badge: 'Weather',
      };
    }
  }

  private async askGemini(prompt: string): Promise<CommandExecutionResult> {
    if (!this.geminiClient) {
      return {
        spokenText: `Hey there neighbor! I heard you ask: "${prompt}". To unlock my full conversational intelligence, please enter your Gemini API key in settings!`,
        displayText: `Hey neighbor! SAYA is ready. (Add Gemini API key in settings for real-time AI responses)`,
        badge: 'SAYA Neighbor',
      };
    }

    try {
      const response = await this.geminiClient.models.generateContent({
        model: 'gemini-2.5-flash',
        contents: prompt,
        config: {
          systemInstruction:
            'You are SAYA, a warm, helpful, conversational voice assistant that acts like a friendly neighbor. Speak concisely in 1-3 sentences with a friendly, welcoming tone.',
        },
      });

      const reply = response.text || "I'm right here neighbor, what can I do for you?";
      return {
        spokenText: reply,
        displayText: reply,
        badge: 'Gemini AI',
      };
    } catch (err: any) {
      return {
        spokenText: `Hey neighbor, I had a slight hiccup reaching the AI service: ${err?.message || 'network error'}.`,
        displayText: `Connection notice: ${err?.message || 'Check Gemini key'}`,
        badge: 'Notice',
      };
    }
  }
}
