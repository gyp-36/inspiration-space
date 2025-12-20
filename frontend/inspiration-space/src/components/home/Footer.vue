<template>
  <footer class="footer">
    <div class="footer-content">
      <div class="footer-section brand-section">
        <h3>知创空间</h3>
        <p>连接创作者与爱好者，激发无限创意。我们致力于打造最优质的数字艺术社区。</p>
        <div class="social-links">
          <!-- 占位社交图标 -->
          <div class="social-icon"></div>
          <div class="social-icon"></div>
          <div class="social-icon"></div>
        </div>
      </div>
      
      <div class="footer-section">
        <h3>平台</h3>
        <ul>
          <li><a href="#home">首页</a></li>
          <li><a href="#works">作品库</a></li>
          <li><a href="#creators">创作者</a></li>
          <li><a href="#events">活动</a></li>
        </ul>
      </div>

      <div class="footer-section">
        <h3>社区</h3>
        <ul>
          <li><a href="#chat">聊天室</a></li>
          <li><a href="#forum">讨论区</a></li>
          <li><a href="#blog">博客</a></li>
          <li><a href="#guidelines">社区公约</a></li>
        </ul>
      </div>

      <div class="footer-section contact-section">
        <h3>联系我们</h3>
        <ul>
          <li>邮箱: contact@zhichuangspace.com</li>
          <li>电话: 400-888-9999</li>
          <li>工作时间: 周一至周日 9:00-18:00</li>
        </ul>
      </div>
    </div>
    <div class="footer-bottom">
      <div class="copyright">
        &copy; 2025 知创空间 版权所有
      </div>
      <div class="beian">
        <a href="http://beian.miit.gov.cn" target="_blank" rel="noopener noreferrer">
          粤ICP备2025492898号-1
        </a>
      </div>
    </div>
  </footer>
</template>

<script setup>
// 无需额外逻辑
</script>

<style scoped>
.footer {
  background: white;
  width: 100%;
  box-sizing: border-box;
  border-top: 1px solid #eaeaea;
  margin-top: auto; /* 确保在Flex布局中推到底部 */
}

.footer-content {
  display: flex;
  justify-content: space-between;
  max-width: 1440px;
  margin: 0 auto;
  padding: 60px 20px 40px;
  gap: 40px;
  flex-wrap: wrap;
}

.footer-section {
  flex: 1;
  min-width: 160px;
}

.brand-section {
  flex: 2; /* 品牌栏稍宽 */
  min-width: 300px;
}

.footer-section h3 {
  color: #1a1a1a;
  margin-bottom: 24px;
  font-size: 16px;
  font-weight: 700;
}

.footer-section p {
  color: #666;
  line-height: 1.6;
  margin: 0 0 20px 0;
  font-size: 14px;
  max-width: 300px;
}

.footer-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-section ul li {
  margin-bottom: 12px;
  color: #666;
  font-size: 14px;
}

.footer-section ul li a {
  color: #666;
  text-decoration: none;
  transition: color 0.2s;
}

.footer-section ul li a:hover {
  color: #0066cc;
}

.social-links {
  display: flex;
  gap: 12px;
}

.social-icon {
  width: 32px;
  height: 32px;
  background: #f0f0f0;
  border-radius: 50%;
  transition: background 0.2s;
}

.social-icon:hover {
  background: #e0e0e0;
}

.footer-bottom {
  max-width: 1440px;
  margin: 0 auto;
  padding: 24px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  color: #999;
  font-size: 13px;
}

.footer-bottom a {
  color: #999;
  text-decoration: none;
  transition: color 0.2s;
}

.footer-bottom a:hover {
  color: #666;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    padding: 40px 20px;
  }
  
  .brand-section {
    margin-bottom: 20px;
  }
  
  .footer-bottom {
    flex-direction: column;
    text-align: center;
  }
}
</style>