import React from 'react';
import { clsx } from 'clsx';

interface BadgeProps {
  children: React.ReactNode;
  variant?: 'easy' | 'medium' | 'hard' | 'admin' | 'user' | 'default';
  color?: string;
  className?: string;
}

const Badge: React.FC<BadgeProps> = ({ children, variant = 'default', color, className }) => {
  let styles = '';

  if (color) {
    // If a custom color is provided (like topic colors), use inline styles
    return (
      <span 
        className={clsx("px-2.5 py-0.5 rounded-full text-xs font-medium border", className)}
        style={{ 
          backgroundColor: `${color}20`, // 20% opacity for bg
          color: color,
          borderColor: `${color}40` // 40% opacity for border
        }}
      >
        {children}
      </span>
    );
  }

  switch (variant) {
    case 'easy':
      styles = 'bg-emerald-100 text-emerald-800 border-emerald-200 dark:bg-emerald-900/30 dark:text-emerald-400 dark:border-emerald-800';
      break;
    case 'medium':
      styles = 'bg-amber-100 text-amber-800 border-amber-200 dark:bg-amber-900/30 dark:text-amber-400 dark:border-amber-800';
      break;
    case 'hard':
      styles = 'bg-red-100 text-red-800 border-red-200 dark:bg-red-900/30 dark:text-red-400 dark:border-red-800';
      break;
    case 'admin':
      styles = 'bg-purple-100 text-purple-800 border-purple-200 dark:bg-purple-900/30 dark:text-purple-400 dark:border-purple-800';
      break;
    case 'user':
      styles = 'bg-blue-100 text-blue-800 border-blue-200 dark:bg-blue-900/30 dark:text-blue-400 dark:border-blue-800';
      break;
    default:
      styles = 'bg-gray-100 text-gray-800 border-gray-200 dark:bg-slate-800 dark:text-slate-300 dark:border-slate-700';
  }

  return (
    <span className={clsx("px-2.5 py-0.5 rounded-full text-xs font-medium border", styles, className)}>
      {children}
    </span>
  );
};

export default Badge;
