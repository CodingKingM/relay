import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { useTheme } from '../../hooks/useTheme.jsx'

const RelayLogo = () => (
    <img src="/favicon-32x32.png" alt="" width="28" height="28" style={{ display: 'block' }} />
)

const HomeIcon = () => (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/>
        <polyline points="9 22 9 12 15 12 15 22"/>
    </svg>
)

const PostsIcon = () => (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
    </svg>
)

const SearchIcon = () => (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="11" cy="11" r="8"/>
        <line x1="21" y1="21" x2="16.65" y2="16.65"/>
    </svg>
)

const ProfileIcon = () => (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
        <circle cx="12" cy="7" r="4"/>
    </svg>
)

const SunIcon = () => (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="5"/>
        <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/>
    </svg>
)

const MoonIcon = () => (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M21 12.79A9 9 0 1111.21 3a7 7 0 109.79 9.79z"/>
    </svg>
)

function Navigation() {
    const { isAuthenticated, logout } = useAuth()
    const navigate = useNavigate()
    const { theme, toggleTheme } = useTheme()

    const handleLogout = async () => {
        await logout()
        navigate('/login')
    }

    const navLinkClass = ({ isActive }) => `nav-link${isActive ? ' active' : ''}`
    const bottomNavClass = ({ isActive }) => `bottom-nav-item${isActive ? ' active' : ''}`

    return (
        <>
        <nav className="navbar" role="navigation" aria-label="Main navigation">
            <div className="nav-container">
                <Link
                    to={isAuthenticated ? '/timeline' : '/login'}
                    className="nav-brand-icon"
                    aria-label="Relay — go to home"
                >
                    <RelayLogo />
                    <span className="nav-brand-text">Relay</span>
                </Link>

                {/* Desktop nav links — hidden on mobile */}
                <ul className="nav-links" id="main-nav-links">
                    {isAuthenticated ? (
                        <>
                        <li><NavLink to="/timeline" className={navLinkClass}>Timeline</NavLink></li>
                        <li><NavLink to="/my-posts" className={navLinkClass}>My Posts</NavLink></li>
                        <li><NavLink to="/search" className={navLinkClass}>Search</NavLink></li>
                        <li><NavLink to="/profile" className={navLinkClass}>Profile</NavLink></li>
                        <li>
                            <button onClick={handleLogout} className="nav-link nav-logout-btn">
                                Logout
                            </button>
                        </li>
                        </>
                    ) : (
                        <>
                        <li><NavLink to="/login" className={navLinkClass}>Login</NavLink></li>
                        <li><NavLink to="/register" className={navLinkClass}>Register</NavLink></li>
                        </>
                    )}
                </ul>

                {/* Right side: theme toggle always visible + unauthenticated links on mobile */}
                <div className="nav-right">
                    {!isAuthenticated && (
                        <div className="nav-mobile-auth">
                            <NavLink to="/login" className={navLinkClass}>Login</NavLink>
                            <NavLink to="/register" className={navLinkClass}>Register</NavLink>
                        </div>
                    )}
                    <button
                        onClick={toggleTheme}
                        className="theme-icon-btn"
                        aria-label={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
                    >
                        {theme === 'dark' ? <SunIcon /> : <MoonIcon />}
                    </button>
                </div>
            </div>
        </nav>

        {/* Bottom navigation — mobile only, authenticated only */}
        {isAuthenticated && (
            <nav className="bottom-nav" role="navigation" aria-label="Mobile navigation">
                <NavLink to="/timeline" className={bottomNavClass} aria-label="Timeline">
                    <HomeIcon />
                    <span>Home</span>
                </NavLink>
                <NavLink to="/my-posts" className={bottomNavClass} aria-label="My Posts">
                    <PostsIcon />
                    <span>Posts</span>
                </NavLink>
                <NavLink to="/search" className={bottomNavClass} aria-label="Search">
                    <SearchIcon />
                    <span>Search</span>
                </NavLink>
                <NavLink to="/profile" className={bottomNavClass} aria-label="My Profile">
                    <ProfileIcon />
                    <span>Profile</span>
                </NavLink>
            </nav>
        )}
        </>
    )
}

export default Navigation
