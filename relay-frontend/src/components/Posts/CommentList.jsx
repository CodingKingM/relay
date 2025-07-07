import { useEffect, useState } from 'react'
import { httpClient } from '../../utils/httpClient'

function CommentList({ postId, currentUser, onCommentDeleted, refresh }) {
    const [comments, setComments] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

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

    useEffect(() => {
        setLoading(true)
        httpClient.get(`/posts/${postId}/comments`)
            .then(setComments)
            .catch(err => setError(err.message))
            .finally(() => setLoading(false))
    }, [postId, refresh])

    const handleDelete = async (commentId) => {
        if (!window.confirm('Delete this comment?')) return
        try {
            await httpClient.delete(`/posts/comments/${commentId}`)
            setComments(comments => comments.filter(c => c.id !== commentId))
            if (onCommentDeleted) onCommentDeleted(commentId)
        } catch (err) {
            alert('Failed to delete comment: ' + err.message)
        }
    }

    if (loading) return <div className="loading">Loading comments...</div>
    if (error) {
        if (error.toLowerCase().includes('not found') || error.toLowerCase().includes('404')) {
            return <div className="error-message">This post no longer exists.</div>
        }
        return <div className="error-message">{error}</div>
    }
    if (!comments.length) return <div className="empty-state">No comments yet.</div>

    return (
        <ul className="comment-list">
            {comments.map(comment => (
                <li key={comment.id} className="comment-item">
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 8 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                            <span className="comment-author" style={{ display: 'inline-block' }}>{comment.username}:</span>
                            <span className="comment-content" style={{ display: 'inline-block', marginTop: '2px' }}>{comment.content}</span>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                            <span className="comment-timestamp" style={{ fontSize: '0.85em', color: 'var(--text-muted, #888)' }}>{formatDate(comment.createdAt)}</span>
                            {currentUser === comment.username && (
                                <button
                                    className="delete-comment-button"
                                    onClick={() => handleDelete(comment.id)}
                                    title="Delete comment"
                                    aria-label="Delete comment"
                                    style={{ color: 'var(--text-muted, #888)' }}
                                >
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                        <path d="M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2m2 0v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6h14zM10 11v6m4-6v6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    </svg>
                                </button>
                            )}
                        </div>
                    </div>
                </li>
            ))}
        </ul>
    )
}

export default CommentList 