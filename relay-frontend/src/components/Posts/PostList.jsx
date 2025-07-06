import PostItem from './PostItem'
import { useAuth } from '../../hooks/useAuth'
import { useState } from 'react'

function PostList({ posts, emptyMessage = 'No posts yet', onPostDeleted }) {
    const { user } = useAuth()
    const [postList, setPostList] = useState(posts)

    const handleDelete = (postId) => {
        setPostList(prev => prev.filter(p => p.id !== postId))
        if (typeof onPostDeleted === 'function') {
            onPostDeleted()
        }
    }

    if (!postList || postList.length === 0) {
        return <div className="empty-state">{emptyMessage}</div>
    }

    return (
        <div className="post-list">
            {postList.map(post => (
                <PostItem
                    key={post.id}
                    post={post}
                    currentUser={user?.username}
                    onDelete={handleDelete}
                />
            ))}
        </div>
    )
}

export default PostList