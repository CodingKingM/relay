import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'

function RegisterForm() {
    const [username, setUsername] = useState('')
    const [password, setPassword] = useState('')
    const [confirmPassword, setConfirmPassword] = useState('')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')

    const { register } = useAuth()
    const navigate = useNavigate()

    const handleSubmit = async (e) => {
        e.preventDefault()
        setError('')

        if (password !== confirmPassword) {
            setError('Passwords do not match')
            return
        }

        if (password.length < 4) {
            setError('Password must be at least 4 characters long')
            return
        }

        setLoading(true)

        try {
            await register(username, password)
            navigate('/timeline')
        } catch (err) {
            setError(err.message || 'Registration failed. Username might already be taken.')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="form-container">
            <h2 className="form-title">Join Relay</h2>

            {error && <div className="error-message">{error}</div>}

            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="username" className="form-label">Username</label>
                    <input
                        type="text"
                        id="username"
                        className="form-input"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
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
                        onChange={(e) => setPassword(e.target.value)}
                        required
                        placeholder="At least 4 characters"
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="confirmPassword" className="form-label">Confirm Password</label>
                    <input
                        type="password"
                        id="confirmPassword"
                        className="form-input"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        required
                        placeholder="Re-enter your password"
                    />
                </div>

                <button
                    type="submit"
                    className="form-button"
                    disabled={loading || !username || !password || !confirmPassword}
                >
                    {loading ? 'Creating account...' : 'Register'}
                </button>
            </form>

            <div className="form-footer">
                Already have an account? {' '}
                <Link to="/login" className="form-link">Login here</Link>
            </div>
        </div>
    )
}

export default RegisterForm