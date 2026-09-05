import React, { useEffect, useState } from 'react';
import { Bookmark as BookmarkIcon } from 'lucide-react';
import { motion } from 'framer-motion';
import { api } from '../../lib/api';
import { useToast } from '../ui/Toast';

interface BookmarkButtonProps {
  taskId: number;
  initialBookmarked?: boolean;
  className?: string;
}

export const BookmarkButton: React.FC<BookmarkButtonProps> = ({
  taskId,
  initialBookmarked,
  className = '',
}) => {
  const [bookmarked, setBookmarked] = useState<boolean>(initialBookmarked ?? false);
  const [loading, setLoading] = useState(false);
  const { show } = useToast();

  useEffect(() => {
    if (initialBookmarked === undefined) {
      api.checkBookmark(taskId)
        .then(res => setBookmarked(res.bookmarked))
        .catch(() => {});
    }
  }, [taskId, initialBookmarked]);

  const toggleBookmark = async (e: React.MouseEvent) => {
    e.stopPropagation();
    e.preventDefault();
    if (loading) return;

    const nextState = !bookmarked;
    setBookmarked(nextState);
    setLoading(true);

    try {
      if (nextState) {
        await api.addBookmark(taskId);
        show('Problem bookmarked ⭐', 'success');
      } else {
        await api.removeBookmark(taskId);
        show('Bookmark removed', 'info');
      }
    } catch (err) {
      setBookmarked(!nextState); // revert
      show('Failed to update bookmark', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <motion.button
      whileHover={{ scale: 1.15 }}
      whileTap={{ scale: 0.85 }}
      onClick={toggleBookmark}
      disabled={loading}
      className={`p-1.5 rounded-lg transition-colors ${
        bookmarked
          ? 'text-amber-500 hover:bg-amber-50 dark:hover:bg-amber-950/30'
          : 'text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-slate-800'
      } ${className}`}
      title={bookmarked ? 'Remove Bookmark' : 'Bookmark this problem'}
    >
      <BookmarkIcon
        className={`w-4 h-4 ${bookmarked ? 'fill-amber-500' : ''}`}
      />
    </motion.button>
  );
};

export default BookmarkButton;
