import { createRouter, createWebHistory } from 'vue-router'
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
    
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/Profile.vue'),
    },
    {
      path: '/forum',
      name: 'forum',
      component: () => import('../views/Forum.vue'),
    },
    {
      path: '/forum/:postId',
      name: 'post-detail',
      component: () => import('../views/PostDetail.vue'),
    },
    {
      path: '/user/:userId',
      name: 'user-profile',
      component: () => import('../views/UserProfile.vue'),
    },
    {
      path: '/help',
      name: 'help',
      component: () => import('../views/Help.vue'),
    },
    {
      path: '/chat',
      name: 'chat',
      component: () => import('../views/ChatContainer.vue'),
    },
    {
      path: '/publish',
      name: 'work-publish',
      component: () => import('../views/WorkPublishView.vue'),
    },
    {
      path: '/work/:workId',
      name: 'work-detail',
      component: () => import('../views/WorkDetail.vue'),
    }
  ],
})

export default router
