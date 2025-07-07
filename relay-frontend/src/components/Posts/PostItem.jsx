// Component developed with claude ai assistance for React patterns and state management
// Used ai for like functionality implementation and UI
// Manually (not entirely) customized for project design and user interaction requirements
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useApiCall } from '../../hooks/useApi'
import CommentList from './CommentList'
import CommentForm from './CommentForm'

function PostItem({ post, currentUser, onDelete }) {
    // DEBUG: Log the post object to see what's actually there
    console.log('PostItem received post:', post);
    console.log('authorUsername:', post.authorUsername);
    console.log('author:', post.author);

    const [liked, setLiked] = useState(post.isLikedByCurrentUser)
    const [likeCount, setLikeCount] = useState(post.likeCount)
    const [loading, setLoading] = useState(false)
    const { call } = useApiCall()
    const [showComments, setShowComments] = useState(false)
    const [refreshComments, setRefreshComments] = useState(0)

    const sanitizeTimestamp = (ts) => {
        return ts.replace(/(\.\d{3})\d+/, '$1')
    }

    const formatDate = (timestamp) => {
        const safeTs = sanitizeTimestamp(timestamp)
        const date   = new Date(safeTs)
        if (isNaN(date)) return timestamp

        const now     = new Date()
        const diffMs  = now - date
        const diffMins  = Math.floor(diffMs / 60000)

        if (diffMins < 1) return 'just now'
        if (diffMins < 60) return `${diffMins}m ago`

        const diffHours = Math.floor(diffMins / 60)
        if (diffHours < 24) return `${diffHours}h ago`

        const diffDays = Math.floor(diffHours / 24)
        if (diffDays < 7) return `${diffDays}d ago`

        return date.toLocaleDateString()
    }

    const handleLike = async () => {
        if (loading) return
        setLoading(true)

        try {
            if (liked) {
                await call(`/posts/${post.id}/like`, 'DELETE')
                setLiked(false)
                setLikeCount(prev => Math.max(0, prev - 1))
            } else {
                await call(`/posts/${post.id}/like`, 'POST')
                setLiked(true)
                setLikeCount(prev => prev + 1)
            }
        } catch (err) {
            console.error('Failed to toggle like:', err)
        } finally {
            setLoading(false)
        }
    }

    const handleDelete = async () => {
        if (loading) return
        if (!window.confirm('Are you sure you want to delete this post?')) return
        setLoading(true)
        try {
            await call(`/posts/${post.id}`, 'DELETE')
            if (typeof onDelete === 'function') {
                onDelete(post.id)
            }
        } catch (err) {
            if (err.message && err.message.toLowerCase().includes('not found')) {
                if (typeof onDelete === 'function') {
                    onDelete(post.id)
                }
            } else {
                alert('Failed to delete post: ' + err.message)
            }
        } finally {
            setLoading(false)
        }
    }

    // Use authorUsername from the backend
    const authorName = post.authorUsername || post.author || 'Unknown'
    const isOwnPost = currentUser === authorName

    return (
        <div className="post-item" style={{ position: 'relative' }}>
            <div className="post-header" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Link to={`/user/${authorName}`} className="post-author">
                    {authorName}
                </Link>
                <span className="post-timestamp" style={{ color: '#7f8c8d', fontSize: '0.95rem' }}>
                    {formatDate(post.createdAt)}
                </span>
            </div>

            <div className="post-content">{post.content}</div>

            <div className="post-actions" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                    <button
                        className={`like-button ${liked ? 'liked' : ''}`}
                        onClick={handleLike}
                        disabled={loading || isOwnPost}
                        title={isOwnPost ? "You can't like your own posts" : ""}
                    >
                        <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                            {liked ? (
                                // Filled heart SVG
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="#e25555" stroke="#e25555" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" style={{ verticalAlign: 'middle' }}>
                                    <path d="M12 21C12 21 4 13.36 4 8.5C4 5.42 6.42 3 9.5 3C11.24 3 12.91 3.81 14 5.08C15.09 3.81 16.76 3 18.5 3C21.58 3 24 5.42 24 8.5C24 13.36 16 21 16 21H12Z" />
                                </svg>
                            ) : (
                                // Outlined heart SVG
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#e25555" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" style={{ verticalAlign: 'middle' }}>
                                    <path d="M12.1 8.64l-.1.1-.11-.11C10.14 6.6 7.1 7.24 5.6 9.28c-1.5 2.04-.44 5.12 2.54 7.05L12 21.35l3.86-5.02c2.98-1.93 4.04-5.01 2.54-7.05-1.5-2.04-4.54-2.68-6.29-.64z" />
                                </svg>
                            )}
                            <span style={{ marginLeft: 4 }}>
                                {likeCount} {likeCount === 1 ? 'like' : 'likes'}
                            </span>
                        </span>
                    </button>
                    <button
                        className="toggle-comments-button"
                        onClick={() => setShowComments(v => !v)}
                        aria-label="Show comments"
                        title="Show comments"
                        style={{ background: 'none', border: 'none', padding: 0, cursor: 'pointer', display: 'flex', alignItems: 'center' }}
                    >
                        {/* Speech bubble SVG icon */}
                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#1976d2" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                        </svg>
                    </button>
                </div>
                {isOwnPost && (
                    <button
                        className="delete-post-button"
                        onClick={handleDelete}
                        disabled={loading}
                        title="Delete this post"
                        aria-label="Delete post"
                        style={{ color: 'var(--text-muted, #888)', background: 'none', border: 'none', padding: 0, marginLeft: 8, cursor: 'pointer' }}
                    >
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                            <path d="M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2m2 0v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6h14zM10 11v6m4-6v6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </button>
                )}
            </div>
            {showComments && (
                <div className="comments-section" style={{ marginTop: '1rem' }}>
                    <CommentForm postId={post.id} onCommentAdded={() => setRefreshComments(c => c + 1)} />
                    <CommentList postId={post.id} currentUser={currentUser} refresh={refreshComments} />
                </div>
            )}
        </div>
    )
}

export default PostItem