# MediCare — Healthcare Frontend

React frontend for the Spring Boot microservices healthcare platform.

## Tech Stack
- React 18 + React Router v6
- Axios (API calls via gateway port 8089)
- react-hot-toast (notifications)
- lucide-react (icons)
- No CSS frameworks — pure CSS variables for theming

## Folder Structure

```
src/
├── api/
│   ├── axiosInstance.js      # Axios base config, JWT interceptors
│   ├── authApi.js            # /api/auth/* calls
│   └── adminApi.js           # /api/admin/* calls
├── components/
│   ├── common/
│   │   ├── Button.jsx
│   │   ├── Input.jsx
│   │   ├── Badge.jsx
│   │   ├── Card.jsx
│   │   ├── Modal.jsx
│   │   └── ProtectedRoute.jsx
│   └── layout/
│       ├── Sidebar.jsx
│       └── DashboardLayout.jsx
├── context/
│   └── AuthContext.jsx       # Global auth state + JWT decode
├── pages/
│   ├── auth/
│   │   ├── LoginPage.jsx
│   │   ├── RegisterPage.jsx  # Patient signup
│   │   ├── DoctorRegisterPage.jsx
│   │   └── UnauthorizedPage.jsx
│   ├── admin/
│   │   ├── AdminDashboard.jsx
│   │   ├── UserManagementPage.jsx
│   │   ├── DoctorApprovalsPage.jsx
│   │   └── SpecializationsPage.jsx
│   ├── doctor/
│   │   └── DoctorDashboard.jsx
│   └── patient/
│       └── PatientDashboard.jsx
├── styles/
│   └── global.css
├── App.jsx
└── index.js
```

## API Endpoints Used

All calls go through the API Gateway at `http://localhost:8089`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/login | Login |
| POST | /api/auth/register | Patient registration |
| POST | /api/auth/register/doctor | Doctor registration |
| GET | /api/admin/users | All users (admin) |
| PUT | /api/admin/users/:id | Update user |
| DELETE | /api/admin/users/:id | Delete user |
| PUT | /api/admin/users/:id/role | Change role |
| PUT | /api/admin/users/:id/approve | Approve doctor |
| GET | /api/admin/specializations | List specializations |
| POST | /api/admin/specializations | Add specialization |

## Getting Started

```bash
npm install
npm start
```

App runs at http://localhost:3000

Default admin credentials: `admin / admin123`

## Role-Based Routing

| Role | Lands on |
|------|----------|
| ADMIN | /admin/dashboard |
| DOCTOR | /doctor/dashboard |
| PATIENT | /patient/dashboard |
