import { useEffect, useState } from 'react'
import { useApiCall } from '../hooks/useApi'
import CreatePost from '../components/Posts/CreatePost'
import PostList from '../components/Posts/PostList'

function TimelinePage() {
    const { call, loading, error } = useApiCall()
    const [posts, setPosts] = useState([])

    const fetchPosts = async () => {
        try {
            const result = await call('/posts/timeline')
            setPosts(result)
        } catch (err) {
            console.error('Failed to fetch timeline:', err)
        }
    }
    useEffect(() => {
        fetchPosts()
    }, [])

    const handlePostCreated = (newPost) => {
        console.log('New post created:', newPost)
        setPosts(prev => [newPost, ...prev])
        setTimeout(() => fetchPosts(), 100)
    }
    const handlePostDeleted = (postId) => {
        setPosts(prev => prev.filter(post => post.id !== postId))
    }

    if (loading && !posts.length) {
        return <div className="loading">Loading timeline...</div>
    }

    if (error) {
        return <div className="error-message">Failed to load timeline: {error}</div>
    }

    return (
        <div>
            <h1 className="page-title">Timeline</h1>
            <CreatePost onPostCreated={handlePostCreated} />
            <PostList
                posts={posts}
                emptyMessage="Your timeline is empty. Follow some users to see their posts!"
                onPostDeleted={handlePostDeleted}
            />
        </div>
    )
}

export default TimelinePage