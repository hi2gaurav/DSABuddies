import React from 'react';
import Card from '../ui/Card';
import { TopicProgress } from '../../types';

interface TopicProgressCardProps {
  topic: TopicProgress;
}

const TopicProgressCard: React.FC<TopicProgressCardProps> = ({ topic }) => {
  const { topicName, topicColor, completed, total, percentage } = topic;
  
  return (
    <Card className="p-4 hover:shadow-md transition-shadow">
      <div className="flex justify-between items-center mb-3">
        <div className="flex items-center gap-2">
          <div 
            className="w-3 h-3 rounded-full" 
            style={{ backgroundColor: topicColor || '#3b82f6' }} 
          />
          <h4 className="font-semibold text-gray-900 dark:text-white truncate" title={topicName}>
            {topicName}
          </h4>
        </div>
        <span className="text-sm font-medium text-gray-500 dark:text-gray-400">
          {percentage}%
        </span>
      </div>
      
      <div className="w-full bg-gray-100 dark:bg-slate-700/50 rounded-full h-2 mb-2">
        <div 
          className="h-2 rounded-full transition-all duration-500" 
          style={{ 
            width: `${percentage}%`,
            backgroundColor: topicColor || '#3b82f6' 
          }}
        />
      </div>
      
      <p className="text-xs text-gray-500 dark:text-gray-400 text-right">
        {completed} / {total} problems
      </p>
    </Card>
  );
};

export default TopicProgressCard;
