import React from 'react';
import { clsx } from 'clsx';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  hover?: boolean;
  onClick?: () => void;
}

const Card: React.FC<CardProps> = ({ children, className, hover = false, onClick }) => {
  return (
    <div 
      className={clsx(
        "bg-white dark:bg-slate-800/80 rounded-xl border border-gray-200 dark:border-slate-700/50 shadow-sm overflow-hidden",
        hover && "transition-all duration-200 hover:shadow-md hover:-translate-y-1 hover:border-blue-200 dark:hover:border-slate-600 cursor-pointer",
        className
      )}
      onClick={onClick}
    >
      {children}
    </div>
  );
};

export default Card;
