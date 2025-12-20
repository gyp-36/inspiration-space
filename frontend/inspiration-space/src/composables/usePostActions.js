//论坛帖子操作组合函数

import { ref } from 'vue';
import { 
  likePost, 
  unlikePost, 
  collectPost, 
  uncollectPost,
  repostPost 
} from '@/services/forumService';

export default function usePostActions(post) {
  // 本地状态管理（从props获取初始值）
  const localLikes = ref(post.likes);
  const localIsLiked = ref(post.isLiked);
  const localFavorites = ref(post.favorites);
  const localIsFavorited = ref(post.isFavorited);
  const localShares = ref(post.shares);

  // 点赞操作
  const toggleLike = async () => {
    const prevLiked = localIsLiked.value;
    const prevLikes = localLikes.value;
    
    // 乐观更新
    localIsLiked.value = !prevLiked;
    localLikes.value = prevLiked ? prevLikes - 1 : prevLikes + 1;

    try {
      await (prevLiked ? unlikePost(post.id) : likePost(post.id));
      return {
        liked: localIsLiked.value,
        likes: localLikes.value
      };
    } catch (error) {
      // 操作失败回滚
      localIsLiked.value = prevLiked;
      localLikes.value = prevLikes;
      throw error;
    }
  };

  // 收藏操作
  const toggleFavorite = async () => {
    const prevFavorited = localIsFavorited.value;
    const prevFavorites = localFavorites.value;
    
    // 乐观更新
    localIsFavorited.value = !prevFavorited;
    localFavorites.value = prevFavorited ? prevFavorites - 1 : prevFavorites + 1;

    try {
      await (prevFavorited ? uncollectPost(post.id) : collectPost(post.id));
      return {
        favorited: localIsFavorited.value,
        favorites: localFavorites.value
      };
    } catch (error) {
      // 操作失败回滚
      localIsFavorited.value = prevFavorited;
      localFavorites.value = prevFavorites;
      throw error;
    }
  };

  // 转发操作
  const repost = async () => {
    try {
      await repostPost(post.id);
      localShares.value += 1;
      return { shares: localShares.value };
    } catch (error) {
      throw error;
    }
  };

  return {
    localLikes,
    localIsLiked,
    localFavorites,
    localIsFavorited,
    localShares,
    toggleLike,
    toggleFavorite,
    repost
  };
}
