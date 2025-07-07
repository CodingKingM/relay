import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { useTheme } from '../../hooks/useTheme.jsx'
import { useState } from 'react'

function Navigation() {
    const { isAuthenticated, user, logout } = useAuth()
    const location = useLocation()
    const navigate = useNavigate()
    const { theme, toggleTheme } = useTheme()
    const [menuOpen, setMenuOpen] = useState(false)

    const handleLogout = async () => {
        await logout()
        navigate('/login')
    }

    const isActive = (path) => location.pathname === path

    return (
        <nav className="navbar" role="navigation" aria-label="Main navigation">
            <div className="nav-container">
                <Link to="/" className="nav-brand">Relay</Link>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <button
                        className="burger-menu"
                        aria-label={menuOpen ? "Close navigation menu" : "Open navigation menu"}
                        aria-controls="main-nav-links"
                        aria-expanded={menuOpen}
                        onClick={() => setMenuOpen(o => !o)}
                    >
                        <span className="burger-bar"></span>
                        <span className="burger-bar"></span>
                        <span className="burger-bar"></span>
                    </button>
                </div>
                <ul className={`nav-links${menuOpen ? ' open' : ''}`} id="main-nav-links" onClick={() => setMenuOpen(false)}>
                    {isAuthenticated ? (
                        <>
                        <li>
                            <Link
                                to="/timeline"
                                className={`nav-link ${isActive('/timeline') ? 'active' : ''}`}
                            >
                                Timeline
                            </Link>
                        </li>
                        <li>
                            <Link
                                to="/my-posts"
                                className={`nav-link ${isActive('/my-posts') ? 'active' : ''}`}
                            >
                                My Posts
                            </Link>
                        </li>
                        <li>
                            <Link
                                to="/search"
                                className={`nav-link ${isActive('/search') ? 'active' : ''}`}
                            >
                                Search Users
                            </Link>
                        </li>
                        <li>
                            <Link
                                to="/profile"
                                className={`nav-link ${isActive('/profile') ? 'active' : ''}`}
                            >
                                My Profile
                            </Link>
                        </li>
                        <li className="nav-actions">
                            <button
                                onClick={handleLogout}
                                className="nav-link"
                                style={{ background: 'none', border: 'none', cursor: 'pointer' }}
                            >
                                Logout
                            </button>
                            <label className="theme-toggle-switch" aria-label="Toggle dark mode">
                                <input
                                    type="checkbox"
                                    checked={theme === 'dark'}
                                    onChange={toggleTheme}
                                    aria-label={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
                                    aria-pressed={theme === 'dark'}
                                />
                                <span className="slider">
                                    <span className="icon sun" aria-hidden="true">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="5" stroke="currentColor" strokeWidth="2"/><path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/></svg>
                                    </span>
                                    <span className="icon moon" aria-hidden="true">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M21 12.79A9 9 0 1111.21 3a7 7 0 109.79 9.79z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/></svg>
                                    </span>
                                </span>
                            </label>
                        </li>
                        </>
                    ) : (
                        <>
                        <li>
                            <Link
                                to="/login"
                                className={`nav-link ${isActive('/login') ? 'active' : ''}`}
                            >
                                Login
                            </Link>
                        </li>
                        <li>
                            <Link
                                to="/register"
                                className={`nav-link ${isActive('/register') ? 'active' : ''}`}
                            >
                                Register
                            </Link>
                        </li>
                        <li>
                            <label className="theme-toggle-switch" aria-label="Toggle dark mode">
                                <input
                                    type="checkbox"
                                    checked={theme === 'dark'}
                                    onChange={toggleTheme}
                                    aria-label={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
                                    aria-pressed={theme === 'dark'}
                                />
                                <span className="slider">
                                    <span className="icon sun" aria-hidden="true">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="5" stroke="currentColor" strokeWidth="2"/><path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/></svg>
                                    </span>
                                    <span className="icon moon" aria-hidden="true">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M21 12.79A9 9 0 1111.21 3a7 7 0 109.79 9.79z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/></svg>
                                    </span>
                                </span>
                            </label>
                        </li>
                        </>
                    )}
                </ul>
                {menuOpen && <div className="nav-backdrop" onClick={() => setMenuOpen(false)}></div>}
            </div>
        </nav>
    )
}

export default Navigation