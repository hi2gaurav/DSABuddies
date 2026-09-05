import React, { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { api } from '../../lib/api';
import {
  X,
  Send,
  Sparkles,
  Trash2,
  Copy,
  Check,
  ChevronDown
} from 'lucide-react';

interface ChatMessage {
  role: 'user' | 'model';
  content: string;
}

export const JeetuBhaiyaChat: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>(() => {
    try {
      const stored = sessionStorage.getItem('dsabuddies_jeetu_chat');
      if (stored) return JSON.parse(stored);
    } catch {}
    return [
      {
        role: 'model',
        content: `Arre bhai! Kaho, kaisa chal raha hai preparation? 🎯\n\nMai hu tumhara **Jeetu Bhaiya**! DSA me kisi question me phas gaye ho, Spring Boot samajh nahi aa raha, ya System Design ka trade-off clear karna hai — bindass pucho. Saath me crack karenge interview! 🔥`
      }
    ];
  });
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [copiedIdx, setCopiedIdx] = useState<number | null>(null);
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    try {
      sessionStorage.setItem('dsabuddies_jeetu_chat', JSON.stringify(messages));
    } catch {}
    scrollToBottom();
  }, [messages, isOpen]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSend = async (textToSend?: string) => {
    const prompt = (textToSend || input).trim();
    if (!prompt || loading) return;

    const userMessage: ChatMessage = { role: 'user', content: prompt };
    const updatedHistory = [...messages, userMessage];
    setMessages(updatedHistory);
    setInput('');
    setLoading(true);

    try {
      const response = await api.chatWithJeetuBhaiya(updatedHistory);
      const botMessage: ChatMessage = {
        role: 'model',
        content: response.reply || 'Arre bhai, thoda sa network issue lag raha hai. Ek baar firse pucho na?'
      };
      setMessages((prev) => [...prev, botMessage]);
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        {
          role: 'model',
          content: 'Arre tension mat le, lagta hai API busy hai ya network slow hai. Firse try karo bhai!'
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleClearChat = () => {
    const initialMsg: ChatMessage[] = [
      {
        role: 'model',
        content: `Naya shuruaat! Kaho bhai, aaj kaunsa topic fodna hai? 🚀`
      }
    ];
    setMessages(initialMsg);
    sessionStorage.setItem('dsabuddies_jeetu_chat', JSON.stringify(initialMsg));
  };

  const copyMessage = (text: string, idx: number) => {
    navigator.clipboard.writeText(text);
    setCopiedIdx(idx);
    setTimeout(() => setCopiedIdx(null), 2000);
  };

  const starterChips = [
    { label: '⚡ LRU Cache in O(1)', prompt: 'Jeetu Bhaiya, LRU Cache ka optimal approach aur Java code samjha do with time complexity.' },
    { label: '☕ Virtual Threads vs Platform Threads', prompt: 'Java 21 Virtual Threads vs Platform Threads me kya difference hai interview ke liye?' },
    { label: '🏛️ System Design: TinyURL', prompt: 'TinyURL shortener design karne ke core components aur trade-offs batao.' },
    { label: '🎯 3 Golden Interview Tips', prompt: 'Tech interview crack karne ke 3 sabse important golden tips kya hain?' },
  ];

  return (
    <>
      {/* Floating Action Button */}
      <div className="fixed bottom-5 right-5 z-40">
        <motion.button
          whileHover={{ scale: 1.08 }}
          whileTap={{ scale: 0.92 }}
          onClick={() => setIsOpen(!isOpen)}
          className="relative group p-3.5 bg-gradient-to-tr from-amber-600 via-amber-500 to-yellow-400 text-slate-950 rounded-full shadow-xl shadow-amber-500/30 flex items-center justify-center border-2 border-amber-200 focus:outline-none"
          title="Ask Jeetu Bhaiya (AI Coding Mentor)"
        >
          {isOpen ? (
            <ChevronDown className="w-6 h-6 stroke-[2.5]" />
          ) : (
            <div className="relative">
              <Sparkles className="w-6 h-6 fill-slate-950 text-slate-950 animate-pulse" />
              {/* Online pulse indicator */}
              <span className="absolute -top-1 -right-1 w-3 h-3 bg-emerald-400 rounded-full ring-2 ring-white animate-ping" />
              <span className="absolute -top-1 -right-1 w-3 h-3 bg-emerald-500 rounded-full ring-2 ring-white" />
            </div>
          )}

          {/* Tooltip badge when closed */}
          {!isOpen && (
            <span className="absolute right-full mr-3 px-3 py-1.5 rounded-xl bg-slate-900/90 backdrop-blur-md text-amber-300 text-xs font-bold whitespace-nowrap shadow-lg border border-amber-500/30 pointer-events-none opacity-0 group-hover:opacity-100 transition-opacity">
              Ask Jeetu Bhaiya 💡
            </span>
          )}
        </motion.button>
      </div>

      {/* Floating Chat Drawer Window */}
      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, y: 20, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 20, scale: 0.95 }}
            transition={{ duration: 0.2, ease: 'easeOut' }}
            className="fixed bottom-20 right-4 sm:right-6 z-50 w-[92vw] sm:w-[420px] h-[580px] max-h-[82vh] bg-white/95 dark:bg-slate-900/95 backdrop-blur-xl rounded-3xl shadow-2xl border-2 border-amber-400/40 dark:border-amber-500/30 flex flex-col overflow-hidden"
          >
            {/* Header */}
            <div className="p-4 bg-gradient-to-r from-amber-500/20 via-orange-500/15 to-indigo-500/10 border-b border-amber-200/50 dark:border-amber-800/30 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="relative">
                  <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-amber-500 to-yellow-300 flex items-center justify-center text-slate-950 font-black shadow-md border border-amber-200 text-base">
                    JB
                  </div>
                  <span className="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-emerald-500 rounded-full ring-2 ring-white dark:ring-slate-900" />
                </div>
                <div>
                  <div className="flex items-center gap-1.5">
                    <h3 className="font-extrabold text-sm text-gray-900 dark:text-white">
                      Jeetu Bhaiya
                    </h3>
                    <span className="text-[10px] px-1.5 py-0.2 rounded bg-amber-100 dark:bg-amber-950/60 text-amber-800 dark:text-amber-300 font-extrabold">
                      CHIEF GURU
                    </span>
                  </div>
                  <p className="text-[11px] text-gray-500 dark:text-gray-400 flex items-center gap-1">
                    <span>Powered by Gemini</span>
                    <Sparkles className="w-2.5 h-2.5 text-amber-500" />
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-1">
                <button
                  onClick={handleClearChat}
                  className="p-1.5 text-gray-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-950/30 rounded-lg transition-colors"
                  title="Clear Conversation"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
                <button
                  onClick={() => setIsOpen(false)}
                  className="p-1.5 text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-slate-800 rounded-lg transition-colors"
                  title="Minimize"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            </div>

            {/* Chat Messages */}
            <div className="flex-1 p-4 overflow-y-auto space-y-3.5 scrollbar-thin scrollbar-thumb-gray-200 dark:scrollbar-thumb-slate-700">
              {messages.map((msg, idx) => {
                const isUser = msg.role === 'user';
                return (
                  <div
                    key={idx}
                    className={`flex items-start gap-2.5 ${isUser ? 'flex-row-reverse' : 'flex-row'}`}
                  >
                    {!isUser && (
                      <div className="w-7 h-7 rounded-xl bg-amber-500/20 text-amber-600 dark:text-amber-400 flex items-center justify-center flex-shrink-0 text-xs font-black border border-amber-500/30 mt-0.5">
                        JB
                      </div>
                    )}

                    <div
                      className={`relative group max-w-[85%] rounded-2xl px-3.5 py-2.5 text-xs leading-relaxed ${
                        isUser
                          ? 'bg-gradient-to-r from-indigo-600 to-indigo-700 text-white rounded-tr-none shadow-sm'
                          : 'bg-gray-100 dark:bg-slate-800 text-gray-800 dark:text-gray-100 rounded-tl-none border border-gray-200/60 dark:border-slate-700/60'
                      }`}
                    >
                      <div className="whitespace-pre-line break-words">
                        {msg.content}
                      </div>

                      {!isUser && (
                        <button
                          onClick={() => copyMessage(msg.content, idx)}
                          className="absolute -bottom-2 -right-2 opacity-0 group-hover:opacity-100 p-1 rounded-md bg-white dark:bg-slate-700 border border-gray-200 dark:border-slate-600 shadow-xs text-gray-500 hover:text-gray-900 dark:hover:text-white transition-opacity"
                          title="Copy message"
                        >
                          {copiedIdx === idx ? (
                            <Check className="w-3 h-3 text-emerald-500" />
                          ) : (
                            <Copy className="w-3 h-3" />
                          )}
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}

              {/* Typing indicator */}
              {loading && (
                <div className="flex items-center gap-2.5">
                  <div className="w-7 h-7 rounded-xl bg-amber-500/20 text-amber-600 dark:text-amber-400 flex items-center justify-center text-xs font-black border border-amber-500/30">
                    JB
                  </div>
                  <div className="bg-gray-100 dark:bg-slate-800 rounded-2xl rounded-tl-none px-4 py-3 border border-gray-200/60 dark:border-slate-700/60 flex items-center gap-1.5">
                    <span className="w-1.5 h-1.5 bg-amber-500 rounded-full animate-bounce [animation-delay:-0.3s]" />
                    <span className="w-1.5 h-1.5 bg-amber-500 rounded-full animate-bounce [animation-delay:-0.15s]" />
                    <span className="w-1.5 h-1.5 bg-amber-500 rounded-full animate-bounce" />
                    <span className="text-[11px] text-gray-500 dark:text-gray-400 ml-1.5 font-medium">
                      Jeetu Bhaiya is guiding...
                    </span>
                  </div>
                </div>
              )}

              <div ref={messagesEndRef} />
            </div>

            {/* Quick Starter Chips */}
            {messages.length <= 2 && (
              <div className="px-3 py-2 bg-gray-50/50 dark:bg-slate-800/40 border-t border-gray-100 dark:border-slate-800 flex items-center gap-1.5 overflow-x-auto scrollbar-none">
                {starterChips.map((chip, i) => (
                  <button
                    key={i}
                    onClick={() => handleSend(chip.prompt)}
                    className="text-[11px] px-2.5 py-1 rounded-full bg-white dark:bg-slate-800 border border-gray-200 dark:border-slate-700 hover:border-amber-400 dark:hover:border-amber-500 text-gray-700 dark:text-gray-300 whitespace-nowrap transition-colors flex-shrink-0"
                  >
                    {chip.label}
                  </button>
                ))}
              </div>
            )}

            {/* Input Form */}
            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleSend();
              }}
              className="p-3 bg-white dark:bg-slate-900 border-t border-gray-200 dark:border-slate-800 flex items-center gap-2"
            >
              <input
                type="text"
                placeholder="Ask Jeetu Bhaiya anything on DSA, Spring, System Design..."
                value={input}
                onChange={(e) => setInput(e.target.value)}
                disabled={loading}
                className="flex-1 px-3.5 py-2.5 text-xs rounded-xl border border-gray-300 dark:border-slate-700 bg-gray-50 dark:bg-slate-800 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-amber-500"
              />

              <button
                type="submit"
                disabled={!input.trim() || loading}
                className="p-2.5 rounded-xl bg-gradient-to-tr from-amber-500 to-yellow-400 hover:from-amber-600 hover:to-yellow-500 text-slate-950 font-bold shadow-md shadow-amber-500/20 disabled:opacity-40 transition-all flex-shrink-0"
                title="Send Message"
              >
                <Send className="w-4 h-4" />
              </button>
            </form>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
};

export default JeetuBhaiyaChat;
