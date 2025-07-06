import { useState } from 'react'
import { useApiCall } from '../../hooks/useApi'

function CreatePost({ onPostCreated }) {
    const [content, setContent] = useState('')
    const [loading, setLoading] = useState(false)
    const { call } = useApiCall()

    const handleSubmit = async (e) => {
        e.preventDefault()

        if (!content.trim()) return

        setLoading(true)
        try {
            const newPost = await call('/posts', 'POST', { content: content.trim() })
            setContent('')
            if (onPostCreated) {
                onPostCreated(newPost)
            }
        } catch (err) {
            console.error('Failed to create post:', err)
        } finally {
            setLoading(false)
        }
    }

    return (
        <form className="post-form" onSubmit={handleSubmit}>
      <textarea
          className="post-textarea"
          placeholder="Relay to the world!"
          value={content}
          onChange={(e) => setContent(e.target.value)}
          maxLength={500}
          disabled={loading}
      />
            <div className="post-form-actions" style={{ justifyContent: 'flex-start' }}>
                <button
                    type="submit"
                    className="post-button"
                    disabled={loading || !content.trim()}
                >
                    {loading ? 'Posting...' : 'Post'}
                </button>
                <span style={{ fontSize: '0.875rem', color: '#7f8c8d' }}>
                    {content.length}/500
                </span>
            </div>
        </form>
    )
}

export default CreatePost