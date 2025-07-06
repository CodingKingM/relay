import { useState } from 'react'
import { httpClient } from '../../utils/httpClient'

function CommentForm({ postId, onCommentAdded }) {
    const [content, setContent] = useState('')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState(null)

    const handleSubmit = async (e) => {
        e.preventDefault()
        if (!content.trim()) return
        setLoading(true)
        setError(null)
        try {
            const comment = await httpClient.post(`/posts/${postId}/comments`, content.trim(), { raw: true })
            setContent('')
            if (onCommentAdded) onCommentAdded(comment)
        } catch (err) {
            setError(err.message)
        } finally {
            setLoading(false)
        }
    }

    return (
        <form className="comment-form" onSubmit={handleSubmit} style={{ marginTop: '1rem' }}>
            <input
                type="text"
                className="comment-input"
                placeholder="Add a comment..."
                value={content}
                onChange={e => setContent(e.target.value)}
                maxLength={500}
                disabled={loading}
                style={{ width: '70%' }}
            />
            <button
                type="submit"
                className="comment-submit"
                disabled={loading || !content.trim()}
                style={{ marginLeft: '0.5rem' }}
            >
                {loading ? 'Posting...' : 'Comment'}
            </button>
            {error && <div className="error-message" style={{ marginTop: '0.5rem' }}>{error}</div>}
        </form>
    )
}

export default CommentForm 