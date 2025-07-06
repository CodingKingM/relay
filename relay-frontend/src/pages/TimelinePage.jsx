import { useApi } from '../hooks/useApi'
import PostList from '../components/Posts/PostList'

function TimelinePage() {
    const { data: posts, loading, error } = useApi('/posts/timeline')

    if (loading) {
        return <div className="loading">Loading timeline...</div>
    }

    if (error) {
        return <div className="error-message">Failed to load timeline: {error}</div>
    }

    return (
        <div>
            <h1 className="page-title">Timeline</h1>
            <PostList
                posts={posts}
                emptyMessage="Your timeline is empty. Follow some users to see their posts!"
            />
        </div>
    )
}

export default TimelinePage