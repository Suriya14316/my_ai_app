/**
 * TranscriptLog: Displays a scrollable chat history styled as Apple dark-mode iMessage bubbles.
 */

import React, { useEffect, useRef } from 'react';
import { Volume2 } from 'lucide-react';

export interface ChatEntry {
  id: string;
  sender: 'user' | 'saya';
  text: string;
  badge?: string;
  timestamp: string;
}

interface TranscriptLogProps {
  entries: ChatEntry[];
  liveTranscript: string;
  onReplaySpeech: (text: string) => void;
}

export const TranscriptLog: React.FC<TranscriptLogProps> = ({
  entries,
  liveTranscript,
  onReplaySpeech,
}) => {
  const bottomRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [entries, liveTranscript]);

  return (
    <div className="flex-1 overflow-y-auto px-4 py-3 space-y-3.5 scrollbar-thin scrollbar-thumb-zinc-800">
      {entries.map((entry) => {
        const isUser = entry.sender === 'user';
        return (
          <div
            key={entry.id}
            className={`flex flex-col ${isUser ? 'items-end' : 'items-start'}`}
          >
            {/* Header: Sender, Badge, Timestamp */}
            <div className="flex items-center gap-1.5 px-1 mb-1 text-[11px]">
              <span className={`font-semibold ${isUser ? 'text-cyan-400' : 'text-purple-400'}`}>
                {isUser ? 'You' : 'SAYA'}
              </span>
              {entry.badge && (
                <span className="px-1.5 py-0.5 rounded bg-purple-900/40 border border-purple-500/30 text-purple-300 text-[9px] font-medium">
                  {entry.badge}
                </span>
              )}
              <span className="text-zinc-500 text-[10px]">{entry.timestamp}</span>
            </div>

            {/* Bubble */}
            <div
              className={`max-w-[85%] rounded-2xl px-4 py-2.5 text-sm leading-relaxed shadow-lg ${
                isUser
                  ? 'bg-gradient-to-r from-blue-700 to-blue-600 text-white rounded-br-sm border border-blue-500/30'
                  : 'bg-zinc-900 text-zinc-100 rounded-bl-sm border border-zinc-800'
              }`}
            >
              <p>{entry.text}</p>

              {!isUser && entry.text && (
                <button
                  onClick={() => onReplaySpeech(entry.text)}
                  className="mt-2 flex items-center gap-1 text-[11px] text-cyan-400 hover:text-cyan-300 transition-colors"
                >
                  <Volume2 size={13} />
                  <span>Replay audio</span>
                </button>
              )}
            </div>
          </div>
        );
      })}

      {/* Live speech recognition preview bubble */}
      {liveTranscript && (
        <div className="flex flex-col items-end animate-pulse">
          <span className="text-cyan-400 text-[11px] mb-1 px-1">Listening live...</span>
          <div className="max-w-[80%] rounded-2xl rounded-br-sm px-4 py-2 text-sm italic bg-blue-950/60 border border-cyan-500/40 text-cyan-200">
            {liveTranscript}
          </div>
        </div>
      )}

      <div ref={bottomRef} />
    </div>
  );
};
