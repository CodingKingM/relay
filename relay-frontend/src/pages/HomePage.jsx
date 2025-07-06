import { useEffect, useState } from 'react'
import { useApiCall } from '../hooks/useApi'
import { useAuth } from '../hooks/useAuth'
import CreatePost from '../components/Posts/CreatePost'
import PostList from '../components/Posts/PostList'

function HomePage() {
    const { user } = useAuth()
    const { call, loading, error } = useApiCall()
    const [posts, setPosts] = useState([])

    const fetchPosts = async () => {
        if (!user?.username) return
        call(`/posts/user/${user.username}`)
            .then(result => {
                setPosts(result)
            })
    }
    useEffect(() => {
        fetchPosts()
    }, [user?.username])

    const handlePostCreated = (newPost) => {
        setPosts(prev => [newPost, ...prev])
        fetchPosts()
    }

    const handlePostDeleted = () => {
        fetchPosts()
    }

    if (loading && !posts.length) {
        return <div className="loading">Loading your posts...</div>
    }

    if (error) {
        return <div className="error-message">Failed to load posts: {error}</div>
    }

    return (
        <div>
            <h1 className="page-title">My Posts</h1>
            <CreatePost onPostCreated={handlePostCreated} />
            <PostList
                posts={posts}
                emptyMessage="You haven't posted anything yet. Share your first thought!"
                onPostDeleted={handlePostDeleted}
            />
        </div>
    )
}

export default HomePage