
const API_BASE = 'http://localhost:8080';

class TaskManager {
    constructor() {
        this.token = localStorage.getItem('token');
        this.currentUser = null;
        this.tasks = [];
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.checkAuthStatus();
        this.updateUI();
    }

    setupEventListeners() {
        // Auth forms
        document.getElementById('loginForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.login();
        });

        document.getElementById('registerForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.register();
        });

        // Task forms
        document.getElementById('taskForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.createTask();
        });

        // Logout
        document.getElementById('logoutBtn')?.addEventListener('click', () => {
            this.logout();
        });

        // Navigation
        document.getElementById('showLogin')?.addEventListener('click', () => {
            this.showSection('auth');
            this.showAuthForm('login');
        });

        document.getElementById('showRegister')?.addEventListener('click', () => {
            this.showSection('auth');
            this.showAuthForm('register');
        });

        document.getElementById('showTasks')?.addEventListener('click', () => {
            this.showSection('tasks');
            this.loadTasks();
        });
    }

    async login() {
        const username = document.getElementById('loginUsername').value;
        const password = document.getElementById('loginPassword').value;

        try {
            const response = await this.apiRequest(`${API_BASE}/auth/login`, 'POST', { username, password });
            this.token = response.data.token;
            this.currentUser = { username: response.data.username, email: response.data.email };
            localStorage.setItem('token', this.token);
            this.showAlert('Login successful!', 'success');
            this.showSection('tasks');
            this.loadTasks();
        } catch (error) {
            this.showAlert(error.message || 'Login failed', 'error');
        }
    }

    async register() {
        const user = {
            username: document.getElementById('registerUsername').value,
            password: document.getElementById('registerPassword').value,
            email: document.getElementById('registerEmail').value
        };

        try {
            await this.apiRequest(`${API_BASE}/auth/register`, 'POST', user);
            this.showAlert('Registration successful! Please login.', 'success');
            this.showAuthForm('login');
        } catch (error) {
            this.showAlert(error.message || 'Registration failed', 'error');
        }
    }

    async loadTasks() {
        try {
            const tasks = await this.apiRequest(`${API_BASE}/tasks`);
            this.tasks = tasks;
            this.renderTasks();
        } catch (error) {
            this.showAlert('Failed to load tasks', 'error');
        }
    }

    async createTask() {
        const task = {
            title: document.getElementById('taskTitle').value,
            description: document.getElementById('taskDescription').value,
            dueDate: document.getElementById('taskDueDate').value,
            priority: document.getElementById('taskPriority').value
        };

        try {
            await this.apiRequest(`${API_BASE}/tasks`, 'POST', task);
            this.showAlert('Task created successfully!', 'success');
            document.getElementById('taskForm').reset();
            this.loadTasks();
        } catch (error) {
            this.showAlert(error.message || 'Failed to create task', 'error');
        }
    }

    async updateTask(id, updates) {
        try {
            await this.apiRequest(`${API_BASE}/tasks/${id}`, 'PUT', updates);
            this.showAlert('Task updated successfully!', 'success');
            this.loadTasks();
        } catch (error) {
            this.showAlert(error.message || 'Failed to update task', 'error');
        }
    }

    async deleteTask(id) {
        if (!confirm('Are you sure you want to delete this task?')) return;

        try {
            await this.apiRequest(`${API_BASE}/tasks/${id}`, 'DELETE');
            this.showAlert('Task deleted successfully!', 'success');
            this.loadTasks();
        } catch (error) {
            this.showAlert(error.message || 'Failed to delete task', 'error');
        }
    }

    async completeTask(id) {
        try {
            await this.apiRequest(`${API_BASE}/tasks/${id}/complete`, 'PATCH');
            this.showAlert('Task marked as complete!', 'success');
            this.loadTasks();
        } catch (error) {
            this.showAlert(error.message || 'Failed to complete task', 'error');
        }
    }

    async apiRequest(url, method = 'GET', data = null) {
        const headers = {
            'Content-Type': 'application/json'
        };

        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }

        const config = {
            method,
            headers
        };

        if (data) {
            config.body = JSON.stringify(data);
        }

        const response = await fetch(url, config);

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Network error' }));
            throw new Error(errorData.message || `HTTP ${response.status}`);
        }

        return response.json();
    }

    renderTasks() {
        const taskList = document.getElementById('taskList');
        taskList.innerHTML = '';

        if (this.tasks.length === 0) {
            taskList.innerHTML = '<p class="no-tasks">No tasks found. Create your first task!</p>';
            return;
        }

        this.tasks.forEach(task => {
            const taskElement = this.createTaskElement(task);
            taskList.appendChild(taskElement);
        });
    }

    createTaskElement(task) {
        const div = document.createElement('div');
        div.className = `task-item priority-${task.priority.toLowerCase()} ${task.completed ? 'status-completed' : 'status-pending'}`;

        div.innerHTML = `
            <div class="task-content">
                <div class="task-title">${task.title}</div>
                <div class="task-description">${task.description || 'No description'}</div>
                <div class="task-meta">
                    Due: ${task.dueDate ? new Date(task.dueDate).toLocaleDateString() : 'No due date'} |
                    Priority: ${task.priority} |
                    Status: ${task.status}
                </div>
            </div>
            <div class="task-actions">
                ${!task.completed ? `<button class="btn" onclick="taskManager.completeTask(${task.id})">Complete</button>` : ''}
                <button class="btn btn-secondary" onclick="taskManager.editTask(${task.id})">Edit</button>
                <button class="btn btn-secondary" onclick="taskManager.deleteTask(${task.id})">Delete</button>
            </div>
        `;

        return div;
    }

    editTask(id) {
        const task = this.tasks.find(t => t.id === id);
        if (!task) return;

        // Populate form with task data
        document.getElementById('taskTitle').value = task.title;
        document.getElementById('taskDescription').value = task.description || '';
        document.getElementById('taskDueDate').value = task.dueDate ? task.dueDate.split('T')[0] : '';
        document.getElementById('taskPriority').value = task.priority;

        // Change form to update mode
        const form = document.getElementById('taskForm');
        const submitBtn = form.querySelector('button[type="submit"]');
        submitBtn.textContent = 'Update Task';

        form.onsubmit = (e) => {
            e.preventDefault();
            this.updateTask(id, {
                title: document.getElementById('taskTitle').value,
                description: document.getElementById('taskDescription').value,
                dueDate: document.getElementById('taskDueDate').value,
                priority: document.getElementById('taskPriority').value
            }).then(() => {
                // Reset form
                form.reset();
                submitBtn.textContent = 'Create Task';
                form.onsubmit = (e) => {
                    e.preventDefault();
                    this.createTask();
                };
            });
        };
    }

    showSection(section) {
        document.querySelectorAll('.section').forEach(el => el.classList.add('hidden'));
        document.getElementById(`${section}Section`)?.classList.remove('hidden');
        this.updateUI();
    }

    showAuthForm(form) {
        document.getElementById('loginForm').classList.add('hidden');
        document.getElementById('registerForm').classList.add('hidden');
        document.getElementById(`${form}Form`).classList.remove('hidden');
    }

    checkAuthStatus() {
        if (this.token && this.token.trim() !== '') {
            this.showSection('tasks');
            this.loadTasks();
        } else {
            this.showSection('auth');
            this.showAuthForm('login');
        }
    }

    logout() {
        this.token = null;
        this.currentUser = null;
        localStorage.removeItem('token');
        this.showSection('auth');
        this.showAuthForm('login');
        this.showAlert('Logged out successfully', 'info');
    }

    updateUI() {
        const isLoggedIn = !!this.token;
        document.getElementById('userInfo')?.classList.toggle('hidden', !isLoggedIn);
        document.getElementById('logoutBtn')?.classList.toggle('hidden', !isLoggedIn);
        document.getElementById('authNav')?.classList.toggle('hidden', isLoggedIn);
        document.getElementById('tasksNav')?.classList.toggle('hidden', !isLoggedIn);

        if (this.currentUser) {
            document.getElementById('currentUser').textContent = this.currentUser.username;
        }
    }

    showAlert(message, type) {
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type}`;
        alertDiv.textContent = message;

        const container = document.querySelector('.container');
        container.insertBefore(alertDiv, container.firstChild);

        setTimeout(() => {
            alertDiv.remove();
        }, 5000);
    }
}

// Initialize the application
const taskManager = new TaskManager();
