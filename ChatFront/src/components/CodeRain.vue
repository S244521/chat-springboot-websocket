<template>
  <div class="code-rain-container">
    <canvas ref="canvasRef"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

const canvasRef = ref(null);

onMounted(() => {
  const canvas = canvasRef.value;
  const ctx = canvas.getContext('2d');

  let width = (canvas.width = window.innerWidth);
  let height = (canvas.height = window.innerHeight);

  // 将屏幕分为多个“列”，每列的宽度为 fontSize
  const fontSize = 18;
  const columns = Math.floor(width / fontSize);

  // 记录每一列雨滴的y坐标
  const rainDrops = new Array(columns).fill(1);

  // 可选的字符集
  const charSet = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890@#$%^&*()_+-=[]{}|;:",./<>?';
  // 模拟《黑客帝国》的片假名效果
  const katakana = 'アァカサタナハマヤャラワガザダバパイィキシチニヒミリヰギジヂビピウゥクスツヌフムユュルグズブヅプエェケセテネヘメレヱゲゼデベペオォコソトノホモヨョロヲゴゾドボポヴッン';
  const characterSet = charSet;

  let animationFrameId;

  const draw = () => {
    // 使用半透明的黑色填充背景，创建拖尾效果
    ctx.fillStyle = 'rgba(0, 0, 0, 0.05)';
    ctx.fillRect(0, 0, width, height);

    // 设置字体颜色和样式
    ctx.fillStyle = '#0F0'; // 亮绿色
    // 添加辉光效果
    ctx.shadowColor = '#000000';
    ctx.shadowBlur = 10;
    ctx.font = `${fontSize}px monospace`;

    for (let i = 0; i < columns; i++) {
      const text = characterSet.charAt(Math.floor(Math.random() * characterSet.length));
      const x = i * fontSize;
      const y = rainDrops[i] * fontSize;

      ctx.fillText(text, x, y);

      // 如果雨滴到达屏幕底部或随机重置
      if (y > height && Math.random() > 0.975) {
        rainDrops[i] = 0;
      }
      rainDrops[i]+=1;
    }

    animationFrameId = requestAnimationFrame(draw);
  };

  draw();

  const handleResize = () => {
    width = canvas.width = window.innerWidth;
    height = canvas.height = window.innerHeight;
  };

  window.addEventListener('resize', handleResize);

  onUnmounted(() => {
    cancelAnimationFrame(animationFrameId);
    window.removeEventListener('resize', handleResize);
  });
});
</script>

<style scoped>
.code-rain-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: #000;
  overflow: hidden;
}

canvas {
  display: block;
}
</style>