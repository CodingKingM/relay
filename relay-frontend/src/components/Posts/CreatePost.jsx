import { useState, useRef } from 'react'
import { useApiCall } from '../../hooks/useApi'

function CreatePost({ onPostCreated }) {
    const [content, setContent] = useState('')
    const [loading, setLoading] = useState(false)
    const { call } = useApiCall()
    const [listening, setListening] = useState(false)
    const recognitionRef = useRef(null)
    const [speechError, setSpeechError] = useState(null)
    const CHAR_LIMIT = 240;
    const charCount = content.length;
    const overCharLimit = charCount > CHAR_LIMIT;

    const startListening = () => {
        setSpeechError(null)
        if (!('webkitSpeechRecognition' in window)) {
            setSpeechError('Speech recognition not supported in this browser.')
            alert('Speech recognition not supported in this browser.')
            return
        }
        const recognition = new window.webkitSpeechRecognition()
        recognition.lang = 'en-US'
        recognition.interimResults = false
        recognition.maxAlternatives = 1
        recognition.onstart = () => { console.log('SpeechRecognition started') }
        recognition.onaudiostart = () => { console.log('Audio capturing started') }
        recognition.onspeechstart = () => { console.log('Speech has been detected') }
        recognition.onspeechend = () => { console.log('Speech has stopped being detected') }
        recognition.onresult = (event) => {
            console.log('SpeechRecognition result:', event)
            const transcript = event.results[0][0].transcript
            setContent((prev) => prev + (prev ? ' ' : '') + transcript)
        }
        recognition.onend = () => { setListening(false); console.log('SpeechRecognition ended') }
        recognition.onerror = (e) => {
            setListening(false)
            setSpeechError('Speech recognition error: ' + e.error)
            console.error('SpeechRecognition error:', e)
        }
        recognition.start()
        setListening(true)
        recognitionRef.current = recognition
    }

    const stopListening = () => {
        recognitionRef.current && recognitionRef.current.stop()
        setListening(false)
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        if (!content.trim() || overCharLimit) return
        setLoading(true)
        try {
            console.log('Creating post with content:', content.trim())
            const newPost = await call('/posts', 'POST', { content: content.trim() })
            console.log('Post created successfully:', newPost)
            setContent('')
            if (onPostCreated) {
                console.log('Calling onPostCreated callback')
                onPostCreated(newPost)
            } else {
                console.log('No onPostCreated callback provided')
            }
        } catch (err) {
            console.error('Failed to create post:', err)
        } finally {
            setLoading(false)
        }
    }

    return (
        <form className="post-form" onSubmit={handleSubmit}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <div style={{ position: 'relative', width: '100%' }}>
                    <textarea
                        className="post-textarea"
                        placeholder="Relay to the world!"
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        maxLength={CHAR_LIMIT}
                        disabled={loading}
                        style={{ width: '100%', minHeight: 80, paddingRight: 60, boxSizing: 'border-box' }}
                    />
                    <button
                        type="button"
                        onClick={listening ? stopListening : startListening}
                        aria-label={listening ? 'Stop voice input' : 'Start voice input'}
                        style={{ position: 'absolute', right: 8, bottom: 32, fontSize: '1.3rem', background: 'none', border: 'none', cursor: 'pointer', padding: 0, display: 'flex', alignItems: 'center', zIndex: 2 }}
                        tabIndex="0"
                    >
                        {listening ? (
                            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true"><rect x="6" y="6" width="12" height="12" rx="2" fill="#1976d2"/></svg>
                        ) : (
                            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M12 15c1.66 0 3-1.34 3-3V6c0-1.66-1.34-3-3-3s-3 1.34-3 3v6c0 1.66 1.34 3 3 3zm5-3c0 2.76-2.24 5-5 5s-5-2.24-5-5H5c0 3.53 2.61 6.43 6 6.92V21h2v-2.08c3.39-.49 6-3.39 6-6.92h-2z" fill="#1976d2"/></svg>
                        )}
                    </button>
                    <span style={{ position: 'absolute', right: 8, bottom: 6, fontSize: '0.92em', color: overCharLimit ? '#e74c3c' : '#555', background: 'transparent', pointerEvents: 'none', zIndex: 1 }}>
                        {charCount} / {CHAR_LIMIT}
                    </span>
                </div>
                <button
                    type="submit"
                    className="post-button"
                    disabled={loading || !content.trim() || overCharLimit}
                    aria-label="Send post"
                    title="Send post"
                    style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'none', border: 'none', borderRadius: 6, padding: '0.75rem 1rem', cursor: 'pointer', height: '40px' }}
                >
                    {loading ? (
                        'Posting...'
                    ) : (
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#1976d2" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                            <path d="M22 2L11 13" />
                            <path d="M22 2L15 22L11 13L2 9L22 2Z" />
                        </svg>
                    )}
                </button>
            </div>
            {overCharLimit && <div style={{ fontSize: '0.95em', color: '#e74c3c', marginTop: '0.3rem' }}>Character limit exceeded!</div>}
            {listening && <div style={{ color: '#1976d2', fontWeight: 'bold', marginTop: 4 }}>Listening...</div>}
            {speechError && <div style={{ color: 'red', marginTop: 4 }}>{speechError}</div>}
        </form>
    )
}

export default CreatePost