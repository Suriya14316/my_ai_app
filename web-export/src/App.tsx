/**
 * SAYA - Production-grade Siri-like Voice Assistant
 * Full Dark Mode Minimalist Apple Intelligence Aesthetic
 * Features:
 * - Two activation commands: "Weak dady is home" & "Hey SAYA"
 * - Dual voice profiles (Voice 1: Iris, Voice 2: Orion)
 * - Siri Tasks & Reminders manager
 */

import React, { useState, useMemo } from 'react';
import { Mic, MicOff, Info, Send, Volume2, CheckCircle, ListTodo, Plus, Trash2, X } from 'lucide-react';
import { VoiceOrb, AssistantState } from './components/VoiceOrb';
import { TranscriptLog, ChatEntry } from './components/TranscriptLog';
import { InfoPanel } from './components/InfoPanel';
import { useSpeechRecognition } from './hooks/useSpeechRecognition';
import { useTextToSpeech } from './hooks/useTextToSpeech';
import { CommandRouter, WebUserTask } from './services/CommandRouter';

export const App: React.FC = () => {
  const [apiKey, setApiKey] = useState<string>('');
  const [showInfo, setShowInfo] = useState<boolean>(false);
  const [showTasks, setShowTasks] = useState<boolean>(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const [manualInput, setManualInput] = useState<string>('');
  const [newTaskInput, setNewTaskInput] = useState<string>('');

  const [tasks, setTasks] = useState<WebUserTask[]>([
    { id: 't1', title: 'Review tomorrow morning schedule', isCompleted: false },
    { id: 't2', title: 'Check home security & smart locks', isCompleted: false },
    { id: 't3', title: 'Call Mom this evening', isCompleted: true },
  ]);

  const [chatHistory, setChatHistory] = useState<ChatEntry[]>([
    {
      id: 'welcome-1',
      sender: 'saya',
      text: "Hello there! I'm SAYA, your personal voice assistant. Say \"Weak dady is home\" or \"Hey SAYA\" to activate, or tap the mic.",
      badge: 'Welcome',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);

  const [state, setState] = useState<AssistantState>('IDLE');

  const router = useMemo(() => new CommandRouter(apiKey), [apiKey]);
  const { speak, stop: stopTts, isSpeaking, activeVoice, toggleVoice } = useTextToSpeech();

  const addTask = (title: string) => {
    if (!title.trim()) return;
    setTasks((prev) => [
      ...prev,
      { id: `task-${Date.now()}`, title: title.trim(), isCompleted: false },
    ]);
  };

  const toggleTask = (id: string) => {
    setTasks((prev) =>
      prev.map((t) => (t.id === id ? { ...t, isCompleted: !t.isCompleted } : t))
    );
  };

  const deleteTask = (id: string) => {
    setTasks((prev) => prev.filter((t) => t.id !== id));
  };

  const completeTaskByTitle = (titlePart: string) => {
    setTasks((prev) =>
      prev.map((t) =>
        t.title.toLowerCase().includes(titlePart.toLowerCase())
          ? { ...t, isCompleted: true }
          : t
      )
    );
  };

  const handleCommand = async (commandText: string) => {
    if (!commandText.trim()) return;

    // Add user message
    const userEntry: ChatEntry = {
      id: `usr-${Date.now()}`,
      sender: 'user',
      text: commandText,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };
    setChatHistory((prev) => [...prev, userEntry]);

    // Assistant Thinking
    setState('THINKING');

    // Route command
    const result = await router.route(
      commandText,
      tasks,
      (newTitle) => addTask(newTitle),
      (compTitle) => completeTaskByTitle(compTitle)
    );

    if (result.toast) {
      setToastMessage(result.toast);
      setTimeout(() => setToastMessage(null), 4500);
    }

    const sayaEntry: ChatEntry = {
      id: `saya-${Date.now()}`,
      sender: 'saya',
      text: result.displayText,
      badge: result.badge,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };
    setChatHistory((prev) => [...prev, sayaEntry]);

    // Speak response
    setState(result.isActivation ? 'ACTIVATED' : 'SPEAKING');
    speak(result.spokenText, () => {
      setState('IDLE');
    });
  };

  const {
    isSupported,
    isListening,
    liveTranscript,
    rmsVolume,
    startListening,
    stopListening,
    toggleWakeWordMode,
    isWakeWordActive,
  } = useSpeechRecognition(
    (cmd) => handleCommand(cmd),
    (err) => setToastMessage(err)
  );

  const toggleMic = () => {
    if (isSpeaking) {
      stopTts();
      setState('IDLE');
      return;
    }
    if (isListening) {
      stopListening();
      setState('IDLE');
    } else {
      startListening();
      setState('LISTENING');
    }
  };

  // Compute status label
  const statusLabel = useMemo(() => {
    if (state === 'ACTIVATED') return 'SAYA Activated & Ready!';
    if (state === 'LISTENING' || isListening) return 'SAYA is listening...';
    if (state === 'THINKING') return 'Thinking...';
    if (state === 'SPEAKING' || isSpeaking) return 'Speaking...';
    return isWakeWordActive ? "Listening for 'Weak dady is home' / 'Hey SAYA'..." : 'Tap to speak';
  }, [state, isListening, isSpeaking, isWakeWordActive]);

  const quickChips = [
    'Weak dady is home',
    'Hey SAYA',
    'Show my tasks',
    'Add task buy coffee',
    'open whatsapp',
    'weather today',
    'what is the time',
    'open youtube',
    'call Mom',
  ];

  return (
    <div className="flex flex-col h-screen w-full bg-[#08090e] text-zinc-100 font-sans select-none overflow-hidden">
      {/* Top Header */}
      <header className="flex flex-col px-6 pt-4 pb-2 border-b border-zinc-900/60">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="text-xl font-bold tracking-wider text-white">SAYA</span>
            <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-purple-900/40 border border-purple-500/30 text-purple-300">
              SIRI ACTIVE
            </span>
          </div>

          <div className="flex items-center gap-2">
            {/* Voice 1 / Voice 2 Switcher */}
            <button
              onClick={toggleVoice}
              className="px-2.5 py-1 rounded-full text-xs font-medium bg-zinc-900 border border-cyan-500/40 text-cyan-400 hover:border-cyan-400 transition-colors"
              title="Toggle between Voice 1 (Iris) and Voice 2 (Orion)"
            >
              Voice: {activeVoice === 'iris' ? 'Iris ♀' : 'Orion ♂'}
            </button>

            {/* Siri Tasks Dialog Button */}
            <button
              onClick={() => setShowTasks(true)}
              className="px-2.5 py-1 rounded-full text-xs font-medium bg-zinc-900 border border-pink-500/40 text-pink-400 hover:border-pink-400 transition-colors flex items-center gap-1"
            >
              <ListTodo size={13} />
              <span>Tasks ({tasks.filter((t) => !t.isCompleted).length})</span>
            </button>

            <button
              onClick={() => setShowInfo(true)}
              className="p-1.5 rounded-full bg-zinc-900 border border-zinc-800 text-zinc-400 hover:text-cyan-400 transition-colors"
              title="Architecture & Demo Notes"
            >
              <Info size={16} />
            </button>
          </div>
        </div>

        {/* Security & Wake-word bar */}
        <div className="flex items-center justify-between mt-2 pt-1 text-[11px] text-zinc-400">
          <div className="flex items-center gap-1.5 text-emerald-400">
            <CheckCircle size={12} />
            <span>Voice Match: Daddy (Authorized)</span>
          </div>
          <button
            onClick={toggleWakeWordMode}
            className={`text-[10px] px-2 py-0.5 rounded-md ${
              isWakeWordActive ? 'bg-emerald-950/60 text-emerald-300' : 'bg-zinc-900 text-zinc-500'
            }`}
          >
            {isWakeWordActive ? '● Wake Listening' : '○ Standby'}
          </button>
        </div>
      </header>

      {/* Dynamic Status Text */}
      <div className="text-center py-2">
        <p
          className={`text-xs uppercase tracking-widest font-semibold transition-all ${
            state === 'ACTIVATED' ? 'text-emerald-400' : 'text-zinc-400'
          }`}
        >
          {statusLabel}
        </p>
      </div>

      {/* Center Voice Orb */}
      <div className="flex justify-center items-center py-2">
        <VoiceOrb
          state={isListening ? 'LISTENING' : state}
          rmsVolume={rmsVolume}
          onClick={toggleMic}
        />
      </div>

      {/* Quick Action Chips */}
      <div className="flex items-center gap-2 px-4 py-2 overflow-x-auto scrollbar-none justify-center">
        {quickChips.map((chip) => {
          const isAct = chip === 'Weak dady is home' || chip === 'Hey SAYA';
          return (
            <button
              key={chip}
              onClick={() => handleCommand(chip)}
              className={`px-3 py-1 rounded-full text-xs shrink-0 transition-all ${
                isAct
                  ? 'bg-emerald-950/80 border border-emerald-500/60 text-emerald-300 font-semibold'
                  : 'bg-zinc-900/90 border border-zinc-800 text-zinc-300 hover:border-cyan-500/50 hover:text-cyan-300'
              }`}
            >
              {chip}
            </button>
          );
        })}
      </div>

      {/* Transcript Log (iMessage dark chat) */}
      <TranscriptLog
        entries={chatHistory}
        liveTranscript={liveTranscript}
        onReplaySpeech={(text) => {
          setState('SPEAKING');
          speak(text, () => setState('IDLE'));
        }}
      />

      {/* Toast notification banner */}
      {toastMessage && (
        <div className="mx-4 mb-2 p-2.5 rounded-xl bg-amber-950/80 border border-amber-500/40 text-amber-200 text-xs text-center shadow-lg transition-all">
          {toastMessage}
        </div>
      )}

      {/* Bottom Bar: Manual Input + Circular Mic Button */}
      <footer className="p-4 bg-[#0a0c13] border-t border-zinc-900/80 flex flex-col items-center gap-3">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            if (manualInput.trim()) {
              handleCommand(manualInput.trim());
              setManualInput('');
            }
          }}
          className="w-full max-w-md flex items-center gap-2"
        >
          <input
            type="text"
            value={manualInput}
            onChange={(e) => setManualInput(e.target.value)}
            placeholder="Type e.g. \"Weak dady is home\" or a command..."
            className="flex-1 bg-zinc-900 border border-zinc-800 rounded-full px-4 py-2 text-xs text-white placeholder-zinc-500 focus:outline-none focus:border-cyan-500"
          />
          <button
            type="submit"
            className="p-2 rounded-full bg-blue-600 text-white hover:bg-blue-500 transition-colors"
          >
            <Send size={15} />
          </button>
        </form>

        {/* Hero Mic Button */}
        <button
          onClick={toggleMic}
          className={`w-16 h-16 rounded-full flex items-center justify-center transition-all shadow-xl ${
            state === 'ACTIVATED'
              ? 'bg-gradient-to-tr from-emerald-500 to-cyan-500 text-white ring-4 ring-emerald-500/40 scale-105'
              : isListening
              ? 'bg-gradient-to-tr from-cyan-500 to-blue-600 text-white ring-4 ring-cyan-500/40 scale-105'
              : isSpeaking
              ? 'bg-gradient-to-tr from-pink-500 to-purple-600 text-white ring-4 ring-pink-500/40'
              : 'bg-zinc-800 text-cyan-400 hover:bg-zinc-700 ring-1 ring-zinc-700'
          }`}
          title={isListening ? 'Stop Listening' : 'Start Listening'}
        >
          {isListening ? <MicOff size={26} /> : <Mic size={26} />}
        </button>
      </footer>

      {/* Tasks Modal */}
      {showTasks && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm">
          <div className="relative w-full max-w-md bg-[#0d1017] border border-zinc-800 rounded-2xl p-5 text-zinc-200 shadow-2xl space-y-4">
            <div className="flex items-center justify-between border-b border-zinc-800 pb-3">
              <div className="flex items-center gap-2">
                <ListTodo className="text-pink-400" size={20} />
                <h3 className="text-base font-bold text-white">Siri Tasks & Reminders</h3>
              </div>
              <button
                onClick={() => setShowTasks(false)}
                className="p-1 rounded-full hover:bg-zinc-800 text-zinc-400 hover:text-white"
              >
                <X size={16} />
              </button>
            </div>

            {/* Add task row */}
            <div className="flex items-center gap-2">
              <input
                type="text"
                value={newTaskInput}
                onChange={(e) => setNewTaskInput(e.target.value)}
                placeholder="Add task or say 'add task...'"
                className="flex-1 bg-zinc-900 border border-zinc-700 rounded-lg px-3 py-1.5 text-xs text-white placeholder-zinc-500 focus:outline-none focus:border-cyan-500"
              />
              <button
                onClick={() => {
                  addTask(newTaskInput);
                  setNewTaskInput('');
                }}
                className="p-2 rounded-lg bg-blue-600 hover:bg-blue-500 text-white"
              >
                <Plus size={14} />
              </button>
            </div>

            {/* Tasks list */}
            <div className="max-h-60 overflow-y-auto space-y-2">
              {tasks.map((task) => (
                <div
                  key={task.id}
                  className="flex items-center justify-between p-2.5 rounded-lg bg-zinc-900/60 border border-zinc-800/80"
                >
                  <div
                    onClick={() => toggleTask(task.id)}
                    className="flex items-center gap-2.5 cursor-pointer flex-1"
                  >
                    <div
                      className={`w-4 h-4 rounded-full border flex items-center justify-center ${
                        task.isCompleted
                          ? 'bg-emerald-500 border-emerald-500'
                          : 'border-zinc-600'
                      }`}
                    >
                      {task.isCompleted && <CheckCircle size={12} className="text-white" />}
                    </div>
                    <span
                      className={`text-xs ${
                        task.isCompleted ? 'line-through text-zinc-500' : 'text-zinc-200'
                      }`}
                    >
                      {task.title}
                    </span>
                  </div>
                  <button
                    onClick={() => deleteTask(task.id)}
                    className="text-zinc-500 hover:text-rose-400 p-1"
                  >
                    <Trash2 size={13} />
                  </button>
                </div>
              ))}
            </div>

            <p className="text-[11px] text-purple-300/80 bg-purple-950/40 border border-purple-800/40 p-2 rounded-lg">
              Voice shortcuts: "show my tasks", "add task prepare meeting", "complete task prepare meeting"
            </p>
          </div>
        </div>
      )}

      {/* Settings / Info Modal */}
      {showInfo && (
        <InfoPanel
          apiKey={apiKey}
          onSaveApiKey={(key) => setApiKey(key)}
          onClose={() => setShowInfo(false)}
        />
      )}
    </div>
  );
};

export default App;
