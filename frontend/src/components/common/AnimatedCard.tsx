import React from 'react';
import { motion, HTMLMotionProps } from 'framer-motion';
import { clsx } from 'clsx';

interface AnimatedCardProps extends HTMLMotionProps<'div'> {
  children: React.ReactNode;
  delay?: number;
  className?: string;
  hoverEffect?: boolean;
}

export const AnimatedCard: React.FC<AnimatedCardProps> = ({
  children,
  delay = 0,
  className = '',
  hoverEffect = true,
  ...props
}) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{
        duration: 0.35,
        delay,
        ease: [0.16, 1, 0.3, 1],
      }}
      whileHover={
        hoverEffect
          ? {
              y: -4,
              transition: { duration: 0.2, ease: 'easeOut' },
            }
          : undefined
      }
      className={clsx(
        'bg-white dark:bg-slate-850 rounded-2xl border border-gray-200/80 dark:border-slate-700/70 shadow-sm dark:shadow-none transition-shadow',
        hoverEffect && 'hover:shadow-xl dark:hover:border-slate-600/80',
        className
      )}
      {...props}
    >
      {children}
    </motion.div>
  );
};

export default AnimatedCard;
