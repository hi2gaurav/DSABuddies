import React from 'react';
import { MessageSquareShare } from 'lucide-react';

interface ShareToWhatsAppProps {
  title?: string;
  message: string;
  variant?: 'button' | 'badge' | 'banner';
  className?: string;
}

export const ShareToWhatsApp: React.FC<ShareToWhatsAppProps> = ({
  title = 'Share on WhatsApp',
  message,
  variant = 'button',
  className = '',
}) => {
  const handleShare = () => {
    const currentUrl = window.location.origin;
    const fullMessage = `${message}\n\n👉 Join our WhatsApp DSA group tracker: ${currentUrl}`;
    const whatsappUrl = `https://api.whatsapp.com/send?text=${encodeURIComponent(fullMessage)}`;
    window.open(whatsappUrl, '_blank', 'noopener,noreferrer');
  };

  if (variant === 'badge') {
    return (
      <button
        onClick={handleShare}
        title="Share this achievement on WhatsApp"
        className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 hover:bg-emerald-500/20 transition-all ${className}`}
      >
        <MessageSquareShare className="w-3.5 h-3.5" />
        <span>Share</span>
      </button>
    );
  }

  if (variant === 'banner') {
    return (
      <div className={`p-4 rounded-xl bg-gradient-to-r from-emerald-600/10 via-teal-600/10 to-transparent border border-emerald-500/20 flex flex-col sm:flex-row items-center justify-between gap-3 ${className}`}>
        <div className="flex items-center gap-3 text-left">
          <div className="p-2.5 rounded-lg bg-emerald-500 text-white shadow-md shadow-emerald-500/20">
            <MessageSquareShare className="w-5 h-5" />
          </div>
          <div>
            <h4 className="font-semibold text-gray-900 dark:text-white text-sm">Keep the Group Motivated! 💬</h4>
            <p className="text-xs text-gray-500 dark:text-gray-400">Share today's progress and streak in your WhatsApp study group.</p>
          </div>
        </div>
        <button
          onClick={handleShare}
          className="w-full sm:w-auto px-4 py-2 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-white text-xs font-semibold shadow-sm transition-all flex items-center justify-center gap-2 flex-shrink-0"
        >
          <MessageSquareShare className="w-4 h-4" />
          <span>Post Update</span>
        </button>
      </div>
    );
  }

  return (
    <button
      onClick={handleShare}
      className={`inline-flex items-center gap-2 px-3.5 py-2 rounded-lg bg-[#25D366] hover:bg-[#1EBE5D] text-white text-sm font-medium shadow-sm hover:shadow-md transition-all ${className}`}
    >
      <MessageSquareShare className="w-4 h-4" />
      <span>{title}</span>
    </button>
  );
};

export default ShareToWhatsApp;
