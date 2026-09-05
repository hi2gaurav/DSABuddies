import confetti from 'canvas-confetti';

export const triggerCelebrationConfetti = () => {
  // Center blast
  confetti({
    particleCount: 80,
    spread: 70,
    origin: { y: 0.65 },
    colors: ['#3b82f6', '#10b981', '#f59e0b', '#ec4899', '#8b5cf6'],
    disableForReducedMotion: true,
  });

  // Secondary firework bursts from sides
  setTimeout(() => {
    confetti({
      particleCount: 40,
      angle: 60,
      spread: 55,
      origin: { x: 0.1, y: 0.7 },
      colors: ['#f59e0b', '#fbbf24', '#fef08a'],
    });
    confetti({
      particleCount: 40,
      angle: 120,
      spread: 55,
      origin: { x: 0.9, y: 0.7 },
      colors: ['#10b981', '#34d399', '#6ee7b7'],
    });
  }, 180);
};

export const triggerStreakFire = () => {
  confetti({
    particleCount: 50,
    spread: 60,
    origin: { y: 0.8 },
    colors: ['#f97316', '#ea580c', '#fbbf24', '#ef4444'],
  });
};
