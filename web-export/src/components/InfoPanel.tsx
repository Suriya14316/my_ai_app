/**
 * InfoPanel: Modal dialog detailing technical trade-offs between
 * Web Speech / Browser API and native Android implementations.
 */

import React, { useState } from 'react';
import { X, Smartphone, AlertTriangle, CheckCircle, Key } from 'lucide-react';

interface InfoPanelProps {
  apiKey: string;
  onSaveApiKey: (key: string) => void;
  onClose: () => void;
}

export const InfoPanel: React.FC<InfoPanelProps> = ({ apiKey, onSaveApiKey, onClose }) => {
  const [inputKey, setInputKey] = useState(apiKey);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
      <div className="relative w-full max-w-lg max-h-[85vh] overflow-y-auto bg-[#0d1017] border border-zinc-800 rounded-2xl p-6 text-zinc-200 shadow-2xl space-y-5">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-zinc-800 pb-3">
          <div className="flex items-center gap-2">
            <Smartphone className="text-purple-400" size={22} />
            <div>
              <h2 className="text-base font-bold text-white">SAYA Project Architecture</h2>
              <p className="text-xs text-zinc-400">College Demo Guide: Browser vs Native Android</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-full hover:bg-zinc-800 text-zinc-400 hover:text-white transition-colors"
          >
            <X size={18} />
          </button>
        </div>

        {/* Persona */}
        <div className="bg-zinc-900/80 border border-zinc-800 rounded-xl p-3.5 space-y-1">
          <h3 className="text-xs font-semibold text-cyan-400">AI Personality: Friendly Neighbor</h3>
          <p className="text-xs text-zinc-300 leading-relaxed">
            SAYA is explicitly conditioned to sound like a warm, helpful neighbor rather than a rigid voice engine. Answers are concise (1-3 sentences), approachable, and polite.
          </p>
        </div>

        {/* Feature trade-off table */}
        <div className="space-y-3">
          <h3 className="text-xs font-semibold text-zinc-300 uppercase tracking-wider">
            Browser Limitations vs Native Android Capabilities
          </h3>

          <div className="bg-zinc-900/50 border border-zinc-800 rounded-xl p-3 space-y-2 text-xs">
            <span className="font-semibold text-pink-400">1. Wake-Word Detection ('Hey SAYA')</span>
            <div className="flex items-start gap-2 text-amber-300">
              <AlertTriangle size={14} className="mt-0.5 shrink-0" />
              <span>Browser: Web Speech API stops on silence; requires continuous restart loop in foreground only.</span>
            </div>
            <div className="flex items-start gap-2 text-emerald-400">
              <CheckCircle size={14} className="mt-0.5 shrink-0" />
              <span>Native Android: Dedicated Foreground Service with Porcupine/Vosk wake-word DSP even with screen off.</span>
            </div>
          </div>

          <div className="bg-zinc-900/50 border border-zinc-800 rounded-xl p-3 space-y-2 text-xs">
            <span className="font-semibold text-pink-400">2. Calling ('call Mom')</span>
            <div className="flex items-start gap-2 text-amber-300">
              <AlertTriangle size={14} className="mt-0.5 shrink-0" />
              <span>Browser: Browser security prevents initiating phone calls; displays limitation toast.</span>
            </div>
            <div className="flex items-start gap-2 text-emerald-400">
              <CheckCircle size={14} className="mt-0.5 shrink-0" />
              <span>Native Android: Dispatches Intent.ACTION_DIAL or directly dials via CALL_PHONE permission.</span>
            </div>
          </div>

          <div className="bg-zinc-900/50 border border-zinc-800 rounded-xl p-3 space-y-2 text-xs">
            <span className="font-semibold text-pink-400">3. Messaging & Contacts</span>
            <div className="flex items-start gap-2 text-amber-300">
              <AlertTriangle size={14} className="mt-0.5 shrink-0" />
              <span>Browser: No access to address book contacts; redirects through wa.me URL queries.</span>
            </div>
            <div className="flex items-start gap-2 text-emerald-400">
              <CheckCircle size={14} className="mt-0.5 shrink-0" />
              <span>Native Android: Queries ContactsContract and launches native SMS or WhatsApp package intent.</span>
            </div>
          </div>
        </div>

        {/* Gemini API Key Config */}
        <div className="space-y-2 pt-2 border-t border-zinc-800">
          <div className="flex items-center gap-1.5 text-xs font-semibold text-zinc-300">
            <Key size={14} className="text-purple-400" />
            <span>Gemini API Key</span>
          </div>
          <input
            type="password"
            value={inputKey}
            onChange={(e) => setInputKey(e.target.value)}
            placeholder="Paste your Gemini API Key"
            className="w-full bg-zinc-900 border border-zinc-700 rounded-lg px-3 py-2 text-xs text-white placeholder-zinc-500 focus:outline-none focus:border-cyan-500"
          />
          <button
            onClick={() => {
              onSaveApiKey(inputKey);
              onClose();
            }}
            className="w-full py-2 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white rounded-lg text-xs font-medium transition-all shadow-md"
          >
            Save Configuration
          </button>
        </div>
      </div>
    </div>
  );
};
