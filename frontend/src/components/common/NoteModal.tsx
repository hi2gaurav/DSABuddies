import React, { useEffect, useState } from 'react';
import { X, Save, FileText, Code2, Trash2, Check, Copy } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { api } from '../../lib/api';
import { useToast } from '../ui/Toast';
import { UserNote } from '../../types';

interface NoteModalProps {
  taskId: number;
  taskTitle: string;
  isOpen: boolean;
  onClose: () => void;
}

export const NoteModal: React.FC<NoteModalProps> = ({
  taskId,
  taskTitle,
  isOpen,
  onClose,
}) => {
  const [content, setContent] = useState('');
  const [codeSnippet, setCodeSnippet] = useState('');
  const [language, setLanguage] = useState('java');
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [copied, setCopied] = useState(false);
  const { show } = useToast();

  useEffect(() => {
    if (isOpen) {
      setLoading(true);
      api.getNote(taskId)
        .then((note: UserNote | null) => {
          if (note && note.id) {
            setContent(note.content || '');
            setCodeSnippet(note.codeSnippet || '');
            setLanguage(note.language || 'java');
          } else {
            setContent('');
            setCodeSnippet('');
            setLanguage('java');
          }
        })
        .catch(() => {})
        .finally(() => setLoading(false));
    }
  }, [isOpen, taskId]);

  const handleSave = async () => {
    setSaving(true);
    try {
      await api.saveNote(taskId, { content, codeSnippet, language });
      show('Note saved successfully! 📝', 'success');
      onClose();
    } catch (err) {
      show('Failed to save note', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Delete this note?')) return;
    try {
      await api.deleteNote(taskId);
      show('Note deleted', 'info');
      setContent('');
      setCodeSnippet('');
      onClose();
    } catch (err) {
      show('Failed to delete note', 'error');
    }
  };

  const copyCode = () => {
    if (!codeSnippet) return;
    navigator.clipboard.writeText(codeSnippet);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs">
        <motion.div
          initial={{ opacity: 0, scale: 0.95, y: 15 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.95, y: 15 }}
          transition={{ duration: 0.2, ease: [0.16, 1, 0.3, 1] }}
          className="glass-card bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/80 dark:border-slate-800 shadow-2xl w-full max-w-2xl overflow-hidden flex flex-col max-h-[90vh]"
        >
          {/* Modal Header */}
          <div className="p-6 border-b border-gray-100 dark:border-slate-800 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-xl bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400">
                <FileText className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-gray-900 dark:text-white">Personal Notes & Solution</h3>
                <p className="text-xs text-gray-500 dark:text-gray-400 truncate max-w-md">{taskTitle}</p>
              </div>
            </div>
            <button
              onClick={onClose}
              className="p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-slate-800 rounded-full transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Modal Body */}
          <div className="p-6 overflow-y-auto space-y-5 flex-1">
            {loading ? (
              <div className="py-12 text-center text-gray-400 text-sm">Loading your notes...</div>
            ) : (
              <>
                {/* Notes Input */}
                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-700 dark:text-gray-300 mb-2">
                    Approach, Complexity & Intuition
                  </label>
                  <textarea
                    rows={4}
                    placeholder="e.g. Used two pointers. Time: O(N), Space: O(1). Key edge case: empty array..."
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                    className="w-full p-3.5 text-sm rounded-xl border border-gray-200 dark:border-slate-700 bg-gray-50/50 dark:bg-slate-800/60 text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-blue-500 transition-all resize-none"
                  />
                </div>

                {/* Code Snippet Input */}
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <label className="text-xs font-bold uppercase tracking-wider text-gray-700 dark:text-gray-300 flex items-center gap-1.5">
                      <Code2 className="w-4 h-4 text-indigo-500" />
                      <span>Code Solution</span>
                    </label>
                    <div className="flex items-center gap-2">
                      <select
                        value={language}
                        onChange={(e) => setLanguage(e.target.value)}
                        className="text-xs py-1 px-2.5 rounded-lg border border-gray-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-300 outline-none"
                      >
                        <option value="java">Java</option>
                        <option value="python">Python</option>
                        <option value="cpp">C++</option>
                        <option value="javascript">JavaScript</option>
                        <option value="go">Go</option>
                      </select>
                      {codeSnippet && (
                        <button
                          type="button"
                          onClick={copyCode}
                          className="text-xs px-2.5 py-1 rounded-lg bg-gray-100 dark:bg-slate-800 text-gray-600 dark:text-gray-300 hover:text-blue-600 flex items-center gap-1 transition-colors"
                        >
                          {copied ? <Check className="w-3.5 h-3.5 text-emerald-500" /> : <Copy className="w-3.5 h-3.5" />}
                          <span>{copied ? 'Copied' : 'Copy'}</span>
                        </button>
                      )}
                    </div>
                  </div>
                  <textarea
                    rows={8}
                    placeholder="Paste clean code snippet here..."
                    value={codeSnippet}
                    onChange={(e) => setCodeSnippet(e.target.value)}
                    className="w-full p-3.5 text-xs font-mono rounded-xl border border-gray-200 dark:border-slate-700 bg-gray-900 text-emerald-400 outline-none focus:ring-2 focus:ring-blue-500 transition-all resize-none"
                  />
                </div>
              </>
            )}
          </div>

          {/* Modal Footer */}
          <div className="p-5 border-t border-gray-100 dark:border-slate-800 bg-gray-50/50 dark:bg-slate-850/50 flex items-center justify-between">
            <div>
              {content || codeSnippet ? (
                <button
                  type="button"
                  onClick={handleDelete}
                  className="px-3 py-2 text-xs font-semibold text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-xl flex items-center gap-1.5 transition-colors"
                >
                  <Trash2 className="w-4 h-4" />
                  <span>Delete Note</span>
                </button>
              ) : null}
            </div>
            <div className="flex items-center gap-2.5">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-xs font-bold text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-slate-800 rounded-xl transition-colors"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleSave}
                disabled={saving}
                className="px-5 py-2 text-xs font-bold bg-blue-600 hover:bg-blue-700 text-white rounded-xl shadow-md shadow-blue-500/20 flex items-center gap-2 transition-all active:scale-95 disabled:opacity-50"
              >
                <Save className="w-4 h-4" />
                <span>{saving ? 'Saving...' : 'Save Note'}</span>
              </button>
            </div>
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  );
};

export default NoteModal;
