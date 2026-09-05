import React, { useEffect, useState, useMemo } from 'react';
import { Bookmark as BookmarkIcon, ExternalLink, Trash2, Search, FileText, Star } from 'lucide-react';
import { motion } from 'framer-motion';
import { api } from '../lib/api';
import { Bookmark } from '../types';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import NoteModal from '../components/common/NoteModal';
import { useToast } from '../components/ui/Toast';
import { toSafeUrl } from '../lib/security';

const BookmarksPage: React.FC = () => {
  const [bookmarks, setBookmarks] = useState<Bookmark[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedNoteTask, setSelectedNoteTask] = useState<{ id: number; title: string } | null>(null);
  const { show } = useToast();

  const fetchBookmarks = async () => {
    try {
      const data = await api.getBookmarks();
      setBookmarks(data);
    } catch (err) {
      show('Failed to load bookmarks', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookmarks();
  }, []);

  const handleRemove = async (taskId: number) => {
    try {
      await api.removeBookmark(taskId);
      setBookmarks(bookmarks.filter(b => b.taskId !== taskId));
      show('Bookmark removed', 'info');
    } catch (err) {
      show('Failed to remove bookmark', 'error');
    }
  };

  const filteredBookmarks = useMemo(() => {
    return bookmarks.filter(b =>
      b.taskTitle.toLowerCase().includes(searchQuery.toLowerCase()) ||
      b.topicName.toLowerCase().includes(searchQuery.toLowerCase())
    );
  }, [bookmarks, searchQuery]);

  if (loading) {
    return (
      <div className="py-24 flex justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-black text-gray-900 dark:text-white tracking-tight flex items-center gap-2.5">
            <span>Bookmarked Problems</span>
            <BookmarkIcon className="w-7 h-7 text-amber-500 fill-amber-500" />
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400 mt-1">
            Your saved problems for revision, mock interviews, and quick lookup
          </p>
        </div>

        {/* Search */}
        <div className="relative max-w-xs w-full">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search bookmarks..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-4 py-2 text-xs rounded-xl border border-gray-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>

      {/* Bookmarks List */}
      {filteredBookmarks.length === 0 ? (
        <Card className="p-12 text-center bg-gray-50 dark:bg-slate-850 border border-gray-200/80 dark:border-slate-800">
          <div className="w-16 h-16 rounded-full bg-amber-500/10 text-amber-500 flex items-center justify-center mx-auto mb-4">
            <BookmarkIcon className="w-8 h-8" />
          </div>
          <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-1">No bookmarked problems</h3>
          <p className="text-xs text-gray-500 dark:text-gray-400 max-w-sm mx-auto">
            {searchQuery ? 'No bookmarks match your search.' : 'Click the bookmark icon on any problem to save it here for fast revision!'}
          </p>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredBookmarks.map((b, idx) => (
            <motion.div
              key={b.id}
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.25, delay: idx * 0.03 }}
              whileHover={{ y: -3 }}
            >
              <Card className="p-5 border border-gray-200/80 dark:border-slate-800 shadow-sm hover:shadow-lg transition-all flex flex-col justify-between h-full bg-white dark:bg-slate-850">
                <div>
                  <div className="flex items-start justify-between gap-3 mb-2">
                    <div className="flex items-center gap-2 flex-wrap">
                      <Badge variant={b.difficulty.toLowerCase() as any}>{b.difficulty}</Badge>
                      <Badge color={b.topicColor}>{b.topicName}</Badge>
                      <span className="text-xs font-bold text-amber-500 flex items-center gap-1">
                        <Star className="w-3 h-3 fill-current" /> {b.xpReward} XP
                      </span>
                    </div>

                    <button
                      onClick={() => handleRemove(b.taskId)}
                      className="p-1.5 text-gray-400 hover:text-rose-600 rounded-lg hover:bg-rose-50 dark:hover:bg-rose-950/20 transition-colors"
                      title="Remove Bookmark"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>

                  <h3 className="text-base font-bold text-gray-900 dark:text-white mb-2 line-clamp-1">
                    {b.taskTitle}
                  </h3>

                  {b.taskDescription && (
                    <p className="text-xs text-gray-500 dark:text-gray-400 line-clamp-2 mb-4">
                      {b.taskDescription}
                    </p>
                  )}
                </div>

                <div className="pt-3 border-t border-gray-100 dark:border-slate-800 flex items-center justify-between gap-2">
                  <button
                    onClick={() => setSelectedNoteTask({ id: b.taskId, title: b.taskTitle })}
                    className="text-xs font-bold text-gray-600 dark:text-gray-400 hover:text-blue-600 dark:hover:text-blue-400 flex items-center gap-1.5 transition-colors"
                  >
                    <FileText className="w-3.5 h-3.5" />
                    <span>Notes</span>
                  </button>

                  <a
                    href={toSafeUrl(b.platformLink)}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-xs font-bold text-blue-600 dark:text-blue-400 flex items-center gap-1 hover:underline"
                  >
                    <span>Open Problem</span>
                    <ExternalLink className="w-3.5 h-3.5" />
                  </a>
                </div>
              </Card>
            </motion.div>
          ))}
        </div>
      )}

      {/* Note modal */}
      {selectedNoteTask && (
        <NoteModal
          taskId={selectedNoteTask.id}
          taskTitle={selectedNoteTask.title}
          isOpen={true}
          onClose={() => setSelectedNoteTask(null)}
        />
      )}
    </div>
  );
};

export default BookmarksPage;
