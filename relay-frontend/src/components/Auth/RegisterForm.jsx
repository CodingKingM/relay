import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'

function getPasswordStrength(password) {
    if (!password) return null
    let score = 0
    if (password.length >= 8) score++
    if (password.length >= 12) score++
    if (/[A-Z]/.test(password)) score++
    if (/[0-9]/.test(password)) score++
    if (/[^A-Za-z0-9]/.test(password)) score++
    if (score <= 1) return { label: 'Weak', color: '#ef4444', width: '25%' }
    if (score === 2) return { label: 'Fair', color: '#f59e0b', width: '50%' }
    if (score === 3) return { label: 'Good', color: '#3b82f6', width: '75%' }
    return { label: 'Strong', color: '#22c55e', width: '100%' }
}

function RegisterForm() {
    const [username, setUsername] = useState('')
    const [password, setPassword] = useState('')
    const [confirmPassword, setConfirmPassword] = useState('')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')

    const { register } = useAuth()
    const navigate = useNavigate()

    const strength = getPasswordStrength(password)

    const handleSubmit = async (e) => {
        e.preventDefault()
        setError('')

        if (password !== confirmPassword) {
            setError('Passwords do not match')
            return
        }

        if (password.length < 8) {
            setError('Password must be at least 8 characters')
            return
        }

        setLoading(true)

        try {
            await register(username, password)
            navigate('/timeline')
        } catch (err) {
            setError(err.message || 'Registration failed. Please try again.')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="form-container">
            <h2 className="form-title">Create account</h2>

            {error && <div className="error-message">{error}</div>}

            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="username" className="form-label">Username</label>
                    <input
                        type="text"
                        id="username"
                        className="form-input"
                        value={username}
                        onChange={(e) => { setUsername(e.target.value); setError('') }}
                        required
                        autoFocus
                        placeholder="Choose a unique username"
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="password" className="form-label">Password</label>
                    <input
                        type="password"
                        id="password"
                        className="form-input"
                        value={password}
                        onChange={(e) => { setPassword(e.target.value); setError('') }}
                        required
                        placeholder="At least 8 characters"
                    />
                    {strength && (
                        <div style={{ marginTop: '0.5rem' }}>
                            <div style={{ height: '4px', borderRadius: '2px', background: '#e5e7eb', overflow: 'hidden' }}>
                                <div style={{
                                    height: '100%',
                                    width: strength.width,
                                    background: strength.color,
                                    transition: 'width 0.3s, background 0.3s',
                                    borderRadius: '2px',
                                }} />
                            </div>
                            <span style={{ fontSize: '0.78rem', color: strength.color, marginTop: '0.2rem', display: 'block' }}>
                                {strength.label}
                            </span>
                        </div>
                    )}
                </div>

                <div className="form-group">
                    <label htmlFor="confirmPassword" className="form-label">Confirm Password</label>
                    <input
                        type="password"
                        id="confirmPassword"
                        className="form-input"
                        value={confirmPassword}
                        onChange={(e) => { setConfirmPassword(e.target.value); setError('') }}
                        required
                        placeholder="Re-enter your password"
                    />
                </div>

                <button
                    type="submit"
                    className="form-button"
                    disabled={loading || !username || !password || !confirmPassword}
                >
                    {loading ? 'Creating account...' : 'Create account'}
                </button>
            </form>

            <div className="form-footer">
                Already have an account?{' '}
                <Link to="/login" className="form-link">Sign in</Link>
            </div>
        </div>
    )
}

export default RegisterForm
