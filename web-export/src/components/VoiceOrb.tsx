/**
 * VoiceOrb: Center Siri / Apple Intelligence inspired glowing orb component.
 *
 * States:
 * - IDLE: Soft, rhythmic breathing animation
 * - ACTIVATED: Vibrant emerald / cyan high-energy aura
 * - LISTENING: Rapid pulse + concentric sound waves reactive to microphone RMS
 * - SPEAKING: Dynamic iridescent magenta / violet color shift
 * - THINKING: Rotating aurora gradient
 */

import React from 'react';

export type AssistantState = 'IDLE' | 'ACTIVATED' | 'LISTENING' | 'THINKING' | 'SPEAKING' | 'ERROR';

interface VoiceOrbProps {
  state: AssistantState;
  rmsVolume: number; // 0 to 1
  onClick: () => void;
}

export const VoiceOrb: React.FC<VoiceOrbProps> = ({ state, rmsVolume, onClick }) => {
  const scale =
    state === 'ACTIVATED'
      ? 1.15
      : state === 'LISTENING'
      ? 1 + rmsVolume * 0.4
      : state === 'SPEAKING'
      ? 1.08
      : 1;

  return (
    <div
      onClick={onClick}
      className="relative flex items-center justify-center w-52 h-52 cursor-pointer select-none group"
      title="Click to speak with SAYA"
    >
      {/* 1. Ambient Background Glow */}
      <div
        className={`absolute inset-0 rounded-full blur-2xl transition-all duration-700 ${
          state === 'ACTIVATED'
            ? 'bg-emerald-500/40 scale-130'
            : state === 'LISTENING'
            ? 'bg-cyan-500/30 scale-125'
            : state === 'SPEAKING'
            ? 'bg-pink-500/35 scale-125'
            : state === 'THINKING'
            ? 'bg-purple-500/30 scale-110 animate-pulse'
            : 'bg-blue-600/20 scale-100 group-hover:bg-blue-600/30'
        }`}
      />

      {/* 2. Concentric Sound Wave Rings */}
      {(state === 'LISTENING' || state === 'SPEAKING' || state === 'ACTIVATED') && (
        <>
          <div
            className={`absolute rounded-full border ${
              state === 'ACTIVATED' ? 'border-emerald-400/50' : 'border-cyan-400/40'
            } animate-ping`}
            style={{
              width: `${140 + rmsVolume * 60}px`,
              height: `${140 + rmsVolume * 60}px`,
              animationDuration: '1.2s',
            }}
          />
          <div
            className="absolute rounded-full border border-indigo-400/30 animate-pulse"
            style={{
              width: `${160 + rmsVolume * 80}px`,
              height: `${160 + rmsVolume * 80}px`,
            }}
          />
        </>
      )}

      {/* 3. Outer Iridescent Aurora Ring */}
      <div
        className={`absolute w-36 h-36 rounded-full p-[3px] transition-transform duration-300 ${
          state === 'THINKING' ? 'animate-spin' : ''
        }`}
        style={{
          transform: `scale(${scale})`,
          background:
            state === 'ACTIVATED'
              ? 'conic-gradient(from 0deg, #10b981, #38bdf8, #a855f7, #10b981)'
              : state === 'SPEAKING'
              ? 'conic-gradient(from 0deg, #ec4899, #a855f7, #38bdf8, #ec4899)'
              : state === 'LISTENING'
              ? 'conic-gradient(from 0deg, #38bdf8, #10b981, #6366f1, #38bdf8)'
              : 'conic-gradient(from 0deg, #6366f1, #a855f7, #38bdf8, #6366f1)',
        }}
      >
        <div className="w-full h-full bg-[#08090e] rounded-full" />
      </div>

      {/* 4. Center Glowing Sphere with Specular Highlight */}
      <div
        className="relative w-32 h-32 rounded-full shadow-2xl flex items-center justify-center transition-transform duration-200"
        style={{
          transform: `scale(${scale})`,
          background:
            state === 'ACTIVATED'
              ? 'radial-gradient(circle at 35% 30%, #ffffff, #10b981 40%, #064e3b 85%)'
              : state === 'SPEAKING'
              ? 'radial-gradient(circle at 35% 30%, #ffffff, #f472b6 40%, #c084fc 70%, #831843 100%)'
              : state === 'LISTENING'
              ? 'radial-gradient(circle at 35% 30%, #ffffff, #38bdf8 40%, #6366f1 70%, #0c4a6e 100%)'
              : state === 'THINKING'
              ? 'radial-gradient(circle at 35% 30%, #ffffff, #c084fc 40%, #ec4899 70%, #4c1d95 100%)'
              : 'radial-gradient(circle at 35% 30%, #ffffff, #38bdf8 30%, #6366f1 65%, #0f172a 100%)',
          boxShadow:
            state === 'ACTIVATED'
              ? '0 0 50px rgba(16, 185, 129, 0.7)'
              : state === 'LISTENING'
              ? '0 0 45px rgba(56, 189, 248, 0.65)'
              : state === 'SPEAKING'
              ? '0 0 45px rgba(236, 72, 153, 0.65)'
              : '0 0 25px rgba(99, 102, 241, 0.35)',
        }}
      >
        <div className="w-8 h-8 rounded-full bg-white/20 blur-[1px] animate-pulse" />
      </div>
    </div>
  );
};
