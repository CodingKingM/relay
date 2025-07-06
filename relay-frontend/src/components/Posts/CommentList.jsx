import { useEffect, useState } from 'react'
import { httpClient } from '../../utils/httpClient'

function CommentList({ postId, currentUser, onCommentDeleted, refresh }) {
    const [comments, setComments] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

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
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                        <div>
                            <span className="comment-author">{comment.username}</span>:
                            <span className="comment-content"> {comment.content}</span>
                            {currentUser === comment.username && (
                                <button
                                    className="delete-comment-button"
                                    onClick={() => handleDelete(comment.id)}
                                    title="Delete comment"
                                >🗑️</button>
                            )}
                        </div>
                        <span className="comment-timestamp">{new Date(comment.createdAt).toLocaleString()}</span>
                    </div>
                </li>
            ))}
        </ul>
    )
}

export default CommentList 