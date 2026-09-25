/**
 * useTextToSpeech: Custom React Hook encapsulating Web SpeechSynthesis API
 * with dual voice profiles (Voice 1: Iris, Voice 2: Orion).
 */

import { useState, useEffect, useCallback, useRef } from 'react';

export type VoiceMode = 'iris' | 'orion';

export function useTextToSpeech() {
  const [isSpeaking, setIsSpeaking] = useState<boolean>(false);
  const [isSupported, setIsSupported] = useState<boolean>(true);
  const [activeVoice, setActiveVoice] = useState<VoiceMode>('iris');
  const synthRef = useRef<SpeechSynthesis | null>(null);

  useEffect(() => {
    if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
      synthRef.current = window.speechSynthesis;
    } else {
      setIsSupported(false);
    }
  }, []);

  const speak = useCallback(
    (text: string, onEnd?: () => void) => {
      if (!synthRef.current) return;

      synthRef.current.cancel();

      const utterance = new SpeechSynthesisUtterance(text);
      if (activeVoice === 'iris') {
        utterance.pitch = 1.18; // Voice 1: Iris (Warm Female Siri tone)
        utterance.rate = 1.0;
      } else {
        utterance.pitch = 0.82; // Voice 2: Orion (Deep Male Siri tone)
        utterance.rate = 0.95;
      }

      utterance.onstart = () => {
        setIsSpeaking(true);
      };

      utterance.onend = () => {
        setIsSpeaking(false);
        onEnd?.();
      };

      utterance.onerror = () => {
        setIsSpeaking(false);
        onEnd?.();
      };

      synthRef.current.speak(utterance);
    },
    [activeVoice]
  );

  const toggleVoice = useCallback(() => {
    setActiveVoice((prev) => (prev === 'iris' ? 'orion' : 'iris'));
  }, []);

  const stop = useCallback(() => {
    if (synthRef.current) {
      synthRef.current.cancel();
      setIsSpeaking(false);
    }
  }, []);

  return {
    isSpeaking,
    isSupported,
    activeVoice,
    toggleVoice,
    speak,
    stop,
  };
}
