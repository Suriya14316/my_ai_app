/**
 * useSpeechRecognition: Custom React Hook encapsulating the Web Speech API.
 *
 * College Project Explanation:
 * Browsers do not support true hardware-level always-on background wake-word listening.
 * This hook simulates continuous wake-word listening by looping the Web Speech API Recognition
 * object in the browser foreground, auto-restarting when recognition ends.
 * A native Android implementation would instead use an Android Foreground Service
 * with a low-power engine like Picovoice Porcupine or Vosk.
 */

import { useState, useEffect, useRef, useCallback } from 'react';

// Declare types for Web Speech API
interface SpeechRecognitionEvent extends Event {
  results: SpeechRecognitionResultList;
}

interface SpeechRecognitionErrorEvent extends Event {
  error: string;
}

export interface UseSpeechRecognitionReturn {
  isSupported: boolean;
  isListening: boolean;
  liveTranscript: string;
  rmsVolume: number;
  startListening: () => void;
  stopListening: () => void;
  toggleWakeWordMode: () => void;
  isWakeWordActive: boolean;
}

export function useSpeechRecognition(
  onCommand: (command: string) => void,
  onErrorNotice?: (msg: string) => void
): UseSpeechRecognitionReturn {
  const [isSupported, setIsSupported] = useState<boolean>(true);
  const [isListening, setIsListening] = useState<boolean>(false);
  const [liveTranscript, setLiveTranscript] = useState<string>('');
  const [rmsVolume, setRmsVolume] = useState<number>(0);
  const [isWakeWordActive, setIsWakeWordActive] = useState<boolean>(false);

  // References to preserve instances across re-renders
  const recognitionRef = useRef<any>(null);
  const audioContextRef = useRef<AudioContext | null>(null);
  const analyserRef = useRef<AnalyserNode | null>(null);
  const micStreamRef = useRef<MediaStream | null>(null);
  const animFrameRef = useRef<number | null>(null);

  // Check browser support on mount
  useEffect(() => {
    const SpeechRecognition =
      (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

    if (!SpeechRecognition) {
      setIsSupported(false);
      return;
    }

    const recognition = new SpeechRecognition();
    recognition.continuous = true;
    recognition.interimResults = true;
    recognition.lang = 'en-US';

    recognition.onstart = () => {
      setIsListening(true);
    };

    recognition.onresult = (event: SpeechRecognitionEvent) => {
      let interim = '';
      let finalTranscript = '';

      for (let i = event.resultIndex; i < event.results.length; i++) {
        const item = event.results[i];
        if (item.isFinal) {
          finalTranscript += item[0].transcript;
        } else {
          interim += item[0].transcript;
        }
      }

      setLiveTranscript(interim);

      if (finalTranscript.trim()) {
        const clean = finalTranscript.trim();
        setLiveTranscript('');

        // If wake-word mode is active, filter for 'Hey SAYA' or 'SAYA'
        if (isWakeWordActive) {
          const lower = clean.toLowerCase();
          if (lower.includes('hey saya') || lower.includes('saya')) {
            const stripped = clean.replace(/hey saya/i, '').replace(/saya/i, '').trim();
            if (stripped) {
              onCommand(stripped);
            }
          }
        } else {
          onCommand(clean);
        }
      }
    };

    recognition.onerror = (event: SpeechRecognitionErrorEvent) => {
      if (event.error === 'not-allowed') {
        onErrorNotice?.('Microphone access was denied. Please allow microphone permissions in browser.');
      }
      setIsListening(false);
    };

    recognition.onend = () => {
      setIsListening(false);
      // Continuous wake-word restart loop
      if (isWakeWordActive) {
        setTimeout(() => {
          try {
            recognition.start();
          } catch {
            // Already started or restarting
          }
        }, 300);
      }
    };

    recognitionRef.current = recognition;

    return () => {
      try {
        recognition.stop();
      } catch {}
    };
  }, [isWakeWordActive, onCommand, onErrorNotice]);

  // Audio Analyser for Real-Time Microphone Volume (reactive sound wave rings)
  const setupAudioAnalyser = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      micStreamRef.current = stream;

      const audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)();
      audioContextRef.current = audioCtx;
      const analyser = audioCtx.createAnalyser();
      analyser.fftSize = 256;
      analyserRef.current = analyser;

      const source = audioCtx.createMediaStreamSource(stream);
      source.connect(analyser);

      const buffer = new Uint8Array(analyser.frequencyBinCount);
      const updateVolume = () => {
        analyser.getByteFrequencyData(buffer);
        let sum = 0;
        for (let i = 0; i < buffer.length; i++) {
          sum += buffer[i];
        }
        const avg = sum / buffer.length;
        const norm = Math.min(1, avg / 80);
        setRmsVolume(norm);
        animFrameRef.current = requestAnimationFrame(updateVolume);
      };
      updateVolume();
    } catch {
      // Audio stream error or blocked
    }
  };

  const cleanupAudioAnalyser = () => {
    if (animFrameRef.current) cancelAnimationFrame(animFrameRef.current);
    micStreamRef.current?.getTracks().forEach((track) => track.stop());
    audioContextRef.current?.close();
    setRmsVolume(0);
  };

  const startListening = useCallback(() => {
    if (recognitionRef.current) {
      try {
        recognitionRef.current.start();
        setupAudioAnalyser();
      } catch {
        // Recognition might already be running
      }
    }
  }, []);

  const stopListening = useCallback(() => {
    if (recognitionRef.current) {
      try {
        recognitionRef.current.stop();
      } catch {}
    }
    cleanupAudioAnalyser();
    setIsListening(false);
  }, []);

  const toggleWakeWordMode = useCallback(() => {
    setIsWakeWordActive((prev) => !prev);
  }, []);

  return {
    isSupported,
    isListening,
    liveTranscript,
    rmsVolume,
    startListening,
    stopListening,
    toggleWakeWordMode,
    isWakeWordActive,
  };
}
