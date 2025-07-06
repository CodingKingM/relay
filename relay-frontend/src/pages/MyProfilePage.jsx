import { useEffect, useState } from 'react'
import { httpClient } from '../utils/httpClient'
import { useAuth } from '../hooks/useAuth'
import PostList from '../components/Posts/PostList'
import { useApi } from '../hooks/useApi'

function MyProfilePage() {
    const { user } = useAuth()
    const [profile, setProfile] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)
    const [edit, setEdit] = useState(false)
    const [form, setForm] = useState({ fullName: '', email: '', biography: '' })
    const [saving, setSaving] = useState(false)
    const [success, setSuccess] = useState(false)

    const BIO_WORD_LIMIT = 100;

    // Fetch posts for the current user
    const username = user?.username
    const { data: posts, loading: postsLoading, error: postsError } = useApi(username ? `/posts/user/${username}` : null, [username])

    useEffect(() => {
        setLoading(true)
        httpClient.get('/users/me')
            .then(data => {
                setProfile(data)
                setForm({
                    fullName: data.fullName || '',
                    email: data.email || '',
                    biography: data.biography || ''
                })
            })
            .catch(err => setError(err.message))
            .finally(() => setLoading(false))
    }, [])

    const handleChange = e => {
        setForm(f => ({ ...f, [e.target.name]: e.target.value }))
    }

    const bioWordCount = form.biography.trim() ? form.biography.trim().split(/\s+/).length : 0;
    const bioOverLimit = bioWordCount > BIO_WORD_LIMIT;

    const handleSave = async e => {
        e.preventDefault()
        setSaving(true)
        setError(null)
        setSuccess(false)
        try {
            const updated = await httpClient.put('/users/me', {
                ...profile,
                ...form
            })
            setProfile(updated)
            setEdit(false)
            setSuccess(true)
        } catch (err) {
            setError(err.message)
        } finally {
            setSaving(false)
        }
    }

    if (loading) return <div className="loading">Loading profile...</div>
    if (error) return <div className="error-message">{error}</div>
    if (!profile) return null

    return (
        <>
        <div className="profile-card">
            <div className="profile-avatar">{profile.username.charAt(0).toUpperCase()}</div>
            <h2 className="form-title" style={{ marginBottom: '1.5rem' }}>My Profile</h2>
            {success && <div className="success-message">Profile updated!</div>}
            {!edit ? (
                <div>
                    <div className="profile-info" style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '0.5rem' }}>{profile.username}</div>
                    <div className="profile-info" style={{ color: '#555' }}>
                        {profile.fullName ? profile.fullName : <span style={{ color: '#aaa', fontStyle: 'italic' }}>Full name not set</span>}
                    </div>
                    <div className="profile-info" style={{ color: '#555' }}>
                        {profile.email ? profile.email : <span style={{ color: '#aaa', fontStyle: 'italic' }}>Email not set</span>}
                    </div>
                    <div className="profile-info" style={{ color: '#555', marginBottom: '1rem' }}>
                        {profile.biography ? profile.biography : <span style={{ color: '#aaa', fontStyle: 'italic' }}>No biography</span>}
                    </div>
                    <button className="form-button" style={{ marginTop: '1rem' }} onClick={() => setEdit(true)}>Edit Profile</button>
                </div>
            ) : (
                <form onSubmit={handleSave}>
                    <div className="form-group">
                        <label className="form-label">Full Name</label>
                        <input
                            type="text"
                            name="fullName"
                            className="form-input"
                            value={form.fullName}
                            onChange={handleChange}
                            maxLength={100}
                        />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Email</label>
                        <input
                            type="email"
                            name="email"
                            className="form-input"
                            value={form.email}
                            onChange={handleChange}
                            maxLength={100}
                        />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Biography</label>
                        <textarea
                            name="biography"
                            className="form-input"
                            value={form.biography}
                            onChange={handleChange}
                            maxLength={500}
                        />
                        <div style={{ fontSize: '0.95em', color: bioOverLimit ? '#e74c3c' : '#555', marginTop: '0.3rem' }}>
                            {bioWordCount} / {BIO_WORD_LIMIT} words
                            {bioOverLimit && <span style={{ marginLeft: '0.5rem' }}>Word limit exceeded!</span>}
                        </div>
                    </div>
                    <button className="form-button" type="submit" disabled={saving || bioOverLimit}>{saving ? 'Saving...' : 'Save'}</button>
                    <button className="form-button" type="button" style={{ marginLeft: '1rem', background: '#aaa' }} onClick={() => setEdit(false)} disabled={saving}>Cancel</button>
                </form>
            )}
            {error && <div className="error-message" style={{ marginTop: '1rem' }}>{error}</div>}
        </div>
        <div>
            <h2 className="page-title" style={{ textAlign: 'center', margin: '2rem 0 1.5rem' }}>My Posts</h2>
            {postsLoading && <div className="loading">Loading posts...</div>}
            {postsError && <div className="error-message">Error loading posts: {postsError}</div>}
            {!postsLoading && !postsError && <PostList posts={posts || []} emptyMessage="You haven't posted anything yet." key={posts?.length} />}
        </div>
        </>
    )
}

export default MyProfilePage 