import React from 'react';
import { clsx } from 'clsx';

interface SkeletonProps {
  className?: string;
  variant?: 'rectangular' | 'circular' | 'text' | 'card';
}

export const Skeleton: React.FC<SkeletonProps> = ({
  className = '',
  variant = 'rectangular',
}) => {
  const variantStyles = {
    rectangular: 'rounded-xl',
    circular: 'rounded-full',
    text: 'h-4 rounded-md',
    card: 'rounded-2xl p-6',
  };

  return (
    <div
      className={clsx(
        'skeleton-shimmer',
        variantStyles[variant],
        className
      )}
    />
  );
};

export const DashboardSkeleton: React.FC = () => {
  return (
    <div className="space-y-8 animate-fade-in">
      {/* Header skeleton */}
      <div className="flex justify-between items-center">
        <div className="space-y-2">
          <Skeleton className="w-64 h-8" />
          <Skeleton className="w-96 h-4" />
        </div>
        <Skeleton className="w-36 h-12 rounded-2xl" />
      </div>

      {/* Main tasks skeleton */}
      <div className="space-y-4">
        <div className="flex justify-between items-center">
          <Skeleton className="w-48 h-6" />
          <Skeleton className="w-60 h-8" />
        </div>
        <div className="bg-white dark:bg-slate-800 rounded-2xl p-6 border border-gray-200 dark:border-slate-700 space-y-4">
          <div className="space-y-2">
            <Skeleton className="w-32 h-5" />
            <Skeleton className="w-72 h-6" />
          </div>
          <div className="divide-y divide-gray-100 dark:divide-slate-700/50">
            {[1, 2, 3].map((i) => (
              <div key={i} className="py-4 flex justify-between items-center">
                <div className="space-y-2">
                  <Skeleton className="w-64 h-5" />
                  <Skeleton className="w-40 h-4" />
                </div>
                <Skeleton className="w-28 h-9 rounded-xl" />
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Bottom widgets skeleton */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Skeleton className="h-60 rounded-2xl" />
        <Skeleton className="h-60 rounded-2xl" />
        <Skeleton className="h-60 rounded-2xl" />
      </div>
    </div>
  );
};

export default Skeleton;
