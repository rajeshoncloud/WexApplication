# Git Repository Setup Instructions

## Current Status

✅ Git is already initialized in your project.  
⚠️ Your branch is behind 'origin/master' by 2039 commits.  
📝 You have many untracked files that need to be committed.

## Quick Start Guide

### Step 1: Review What Will Be Committed

```bash
git status
```

This shows:
- **Changes to be committed**: Files already staged
- **Changes not staged for commit**: Modified files not staged
- **Untracked files**: New files not yet in Git

### Step 2: Add Your Project Files

Add all your project files to Git:

```bash
# Add all files in current directory
git add .

# Or add specific important files first:
git add pom.xml
git add src/
git add docker-compose.yml
git add Dockerfile
git add init.sql
git add nginx.conf
git add README.md
git add API_DOCUMENTATION.md
git add SWAGGER_GUIDE.md
git add .gitignore
git add ui/
```

### Step 3: Verify What's Staged

```bash
git status
```

Make sure you see your project files listed under "Changes to be committed".  
The `target/` directory should NOT be included (it's in `.gitignore`).

### Step 4: Commit Your Changes

Create your initial commit:

```bash
git commit -m "Initial commit: Purchase Application

- Spring Boot REST API for managing purchases
- API key authentication system
- Currency conversion using U.S. Treasury API
- Swagger/OpenAPI documentation
- Docker support with MySQL database
- Comprehensive test suite"
```

### Step 5: Handle Remote Repository

You have two options:

#### Option A: Push to Existing Remote (if you want to keep the existing remote)

```bash
# First, pull and merge (if needed)
git pull origin master --allow-unrelated-histories

# Resolve any conflicts if they occur, then:
git add .
git commit -m "Merge with remote repository"

# Push your changes
git push -u origin master
```

#### Option B: Create a New Remote Repository (Recommended for a fresh start)

1. **Create a new repository on GitHub/GitLab/Bitbucket:**
   - Go to your Git provider (GitHub, GitLab, etc.)
   - Create a new repository (don't initialize with README)
   - Copy the repository URL

2. **Remove old remote and add new one:**
   ```bash
   # Check current remote
   git remote -v
   
   # Remove old remote
   git remote remove origin
   
   # Add new remote (replace with your repository URL)
   git remote add origin https://github.com/yourusername/WexApp.git
   
   # Push to new remote
   git push -u origin master
   ```

## Detailed Instructions

### Creating a New Repository on GitHub

1. Go to [GitHub](https://github.com) and sign in
2. Click the **"+"** icon → **"New repository"**
3. Fill in:
   - **Repository name**: `WexApp` (or your choice)
   - **Description**: "Purchase Application with Currency Conversion"
   - **Visibility**: Public or Private
   - **DO NOT** check "Initialize with README" (you already have one)
4. Click **"Create repository"**
5. Copy the repository URL (HTTPS or SSH)

### Setting Up Remote

```bash
# View current remotes
git remote -v

# If you want to update the existing remote URL:
git remote set-url origin https://github.com/yourusername/WexApp.git

# Or if you want to add a new remote:
git remote add origin https://github.com/yourusername/WexApp.git

# For SSH (if you have SSH keys set up):
git remote add origin git@github.com:yourusername/WexApp.git
```

### Pushing Your Code

```bash
# First push (sets upstream branch)
git push -u origin master

# If your default branch is 'main' instead:
git push -u origin main

# Future pushes (after setting upstream)
git push
```

## Complete Workflow Example

Here's a complete example of committing and pushing your code:

```bash
# 1. Check current status
git status

# 2. Add all project files
git add .

# 3. Verify what's staged
git status

# 4. Commit with descriptive message
git commit -m "Initial commit: Purchase Application with API key authentication and currency conversion"

# 5. Check remote (if you want to update it)
git remote -v

# 6. Update or add remote
git remote set-url origin https://github.com/yourusername/WexApp.git
# OR
git remote add origin https://github.com/yourusername/WexApp.git

# 7. Push to remote
git push -u origin master
```

## Important Files to Commit

Make sure these files are included:

✅ **Source Code:**
- `src/` directory (all Java files)
- `pom.xml` (Maven configuration)

✅ **Configuration:**
- `docker-compose.yml`
- `Dockerfile`
- `init.sql`
- `nginx.conf`
- `src/main/resources/application.properties`

✅ **Documentation:**
- `README.md`
- `API_DOCUMENTATION.md`
- `SWAGGER_GUIDE.md`
- `DEBUGGING_GUIDE.md`
- `DOCKER_SETUP.md`
- `LOGGING_GUIDE.md`
- `GIT_SETUP_INSTRUCTIONS.md`

✅ **Frontend:**
- `ui/` directory (HTML files)

✅ **Configuration Files:**
- `.gitignore`
- `.dockerignore` (if exists)

❌ **Should NOT be committed:**
- `target/` (build artifacts - already in .gitignore)
- IDE files (`.idea/`, `.vscode/`, etc. - already in .gitignore)
- Log files
- Environment-specific config files with secrets

## Troubleshooting

### Issue: "Your branch is behind 'origin/master' by 2039 commits"

**This means your local branch has diverged from the remote.** Options:

**Option 1: Start Fresh (Recommended if remote has unrelated code)**
```bash
# Create a new branch for your work
git checkout -b main

# Push new branch
git push -u origin main
```

**Option 2: Merge with Remote**
```bash
# Pull and merge
git pull origin master --allow-unrelated-histories

# Resolve conflicts if any, then:
git add .
git commit -m "Merge with remote repository"
git push
```

### Issue: "fatal: remote origin already exists"

```bash
# Check what it's set to
git remote -v

# Update the URL
git remote set-url origin <new-url>

# Or remove and re-add
git remote remove origin
git remote add origin <new-url>
```

### Issue: Authentication Failed

**For HTTPS:**
- Use a Personal Access Token instead of password
- GitHub: Settings → Developer settings → Personal access tokens

**For SSH:**
- Set up SSH keys in your Git provider
- GitHub: Settings → SSH and GPG keys

### Issue: Large Files or Build Artifacts

```bash
# Remove already tracked files from Git (but keep locally)
git rm -r --cached target/

# Update .gitignore if needed, then commit
git add .gitignore
git commit -m "Remove build artifacts from tracking"
```

## Best Practices

1. ✅ **Commit Often**: Small, logical commits are better than large ones
2. ✅ **Write Good Messages**: Be descriptive about what and why
3. ✅ **Review Before Committing**: Use `git status` and `git diff`
4. ✅ **Use Branches**: Create feature branches for new work
5. ✅ **Keep .gitignore Updated**: Add new build artifacts as needed
6. ❌ **Never Commit Secrets**: No API keys, passwords, or sensitive data

## Verification Checklist

After pushing, verify:

- [ ] All source files are in the repository
- [ ] Documentation files are included
- [ ] Configuration files are present
- [ ] `target/` directory is NOT in the repository
- [ ] `.gitignore` is working correctly
- [ ] README.md is visible on the remote
- [ ] You can clone the repository successfully

## Next Steps After Setup

1. **Protect Main Branch**: Set branch protection rules in your Git provider
2. **Add CI/CD**: Set up GitHub Actions, GitLab CI, etc.
3. **Add Collaborators**: Invite team members
4. **Create Issues**: Track bugs and features
5. **Set Up Branching Strategy**: Use feature branches, develop, main, etc.

## Quick Reference Commands

```bash
# Status and information
git status                    # Check what's changed
git log                       # View commit history
git remote -v                 # View remote repositories

# Staging and committing
git add <file>                # Add specific file
git add .                     # Add all changes
git commit -m "message"       # Commit changes

# Remote operations
git remote add origin <url>  # Add remote
git push -u origin master     # Push and set upstream
git pull                      # Pull latest changes

# Branching
git branch                    # List branches
git checkout -b <name>        # Create and switch branch
git checkout <name>           # Switch branch
```

---

**Need Help?** If you encounter any issues, check the troubleshooting section above or refer to the Git documentation.
