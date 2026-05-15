# Contributing to NetSpeed Indicator

First off, thank you for considering contributing to NetSpeed Indicator! It's people like you that make this project such a great tool.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Architecture Guidelines](#architecture-guidelines)

## 📜 Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to ronyaburaihan@gmail.com.

### Our Standards

- **Be Respectful**: Treat everyone with respect and kindness
- **Be Collaborative**: Work together and help each other
- **Be Professional**: Keep discussions focused and constructive
- **Be Inclusive**: Welcome newcomers and diverse perspectives

## 🤝 How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues to avoid duplicates. When you create a bug report, include as many details as possible:

**Bug Report Template:**
```markdown
**Describe the bug**
A clear and concise description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

**Expected behavior**
A clear and concise description of what you expected to happen.

**Screenshots**
If applicable, add screenshots to help explain your problem.

**Device Information:**
 - Device: [e.g. Pixel 6]
 - Android Version: [e.g. Android 13]
 - App Version: [e.g. 1.0]

**Additional context**
Add any other context about the problem here.
```

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, include:

- **Clear title and description**
- **Step-by-step description** of the suggested enhancement
- **Explain why** this enhancement would be useful
- **List alternatives** you've considered

### Pull Requests

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 🛠️ Development Setup

### Prerequisites

- Android Studio Ladybug or later
- JDK 11 or later
- Android SDK 28 or later
- Git

### Setup Steps

1. **Fork and Clone**
   ```bash
   git clone https://github.com/ronyaburaihan/NetSpeedIndicator.git
   cd NetSpeedIndicator
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Wait for Gradle sync to complete
   - Resolve any dependency issues

4. **Run the App**
   ```bash
   ./gradlew installDebug
   ```

### Project Structure

```
app/src/main/java/com/englesoft/netspeedindicator/
├── presentation/    # UI Layer
├── domain/         # Business Logic
├── data/           # Data Layer
└── core/           # Utilities
```

## 📝 Coding Standards

### Kotlin Style Guide

Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

#### Naming Conventions

```kotlin
// Classes: PascalCase
class SpeedMonitorService

// Functions: camelCase
fun calculateSpeed()

// Variables: camelCase
val currentSpeed = 0L

// Constants: UPPER_SNAKE_CASE
const val MAX_SPEED = 1000L

// Private properties: _camelCase
private val _uiState = MutableStateFlow()
```

#### Code Organization

```kotlin
class MyClass {
    // 1. Companion object
    companion object {
        private const val TAG = "MyClass"
    }
    
    // 2. Properties
    private val property1 = ""
    
    // 3. Init blocks
    init {
        // Initialization
    }
    
    // 4. Public functions
    fun publicFunction() {}
    
    // 5. Private functions
    private fun privateFunction() {}
}
```

### Clean Architecture Principles

#### Layer Separation

```kotlin
// ❌ Don't: Domain depending on Data
class UseCase(private val repository: RepositoryImpl)

// ✅ Do: Domain depending on abstraction
class UseCase(private val repository: Repository)
```

#### Result Wrapper

```kotlin
// ✅ Always use Result wrapper for operations that can fail
suspend fun getData(): Result<Data> {
    return try {
        val data = dataSource.fetch()
        Result.Success(data)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Unknown error")
    }
}
```

#### ViewModel Pattern

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val useCase: UseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = useCase()) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            data = result.data
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                is Result.Loading -> {
                    // Already loading
                }
            }
        }
    }
}
```

### Documentation

#### KDoc Comments

```kotlin
/**
 * Monitors network speed in real-time.
 *
 * This service runs in the foreground and provides continuous
 * monitoring of download and upload speeds.
 *
 * @property speedDataSource The data source for speed information
 * @property usageRepository Repository for storing usage data
 */
class SpeedMonitorService @Inject constructor(
    private val speedDataSource: SpeedDataSource,
    private val usageRepository: UsageRepository
) : Service()
```

#### Inline Comments

```kotlin
// Use inline comments for complex logic
fun calculateSpeed(bytes: Long, timeMs: Long): Long {
    // Convert to bytes per second
    // Formula: (bytes * 1000) / timeMs
    return if (timeMs > 0) {
        (bytes * 1000) / timeMs
    } else {
        0L
    }
}
```

## 📝 Commit Guidelines

### Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Types

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, etc.)
- **refactor**: Code refactoring
- **test**: Adding or updating tests
- **chore**: Maintenance tasks

#### Examples

```bash
feat(home): add real-time speed monitoring

Implement real-time speed tracking using TrafficStats API.
Updates notification every second with current speed.

Closes #123
```

```bash
fix(service): prevent crash on network change

Add null checks for network capabilities to prevent
NullPointerException when network state changes.

Fixes #456
```

## 🔄 Pull Request Process

### Before Submitting

1. **Update Documentation**
   - Update README.md if needed
   - Add KDoc comments to new code
   - Update CHANGELOG.md

2. **Run Tests**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

3. **Check Code Style**
   ```bash
   ./gradlew ktlintCheck
   ```

4. **Build Successfully**
   ```bash
   ./gradlew build
   ```

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] No new warnings generated
- [ ] Tests pass locally

## Screenshots (if applicable)
Add screenshots here

## Related Issues
Closes #(issue number)
```

### Review Process

1. **Automated Checks**: CI/CD runs tests and checks
2. **Code Review**: Maintainers review your code
3. **Feedback**: Address any requested changes
4. **Approval**: Once approved, PR will be merged

## 🏗️ Architecture Guidelines

### Adding a New Feature

Follow this order:

1. **Domain Layer**
   ```kotlin
   // 1. Define domain model
   data class MyData(val id: String, val name: String)
   
   // 2. Create repository interface
   interface MyRepository {
       suspend fun getData(): Result<MyData>
   }
   
   // 3. Create use case
   class GetDataUseCase @Inject constructor(
       private val repository: MyRepository
   ) {
       suspend operator fun invoke(): Result<MyData> {
           return repository.getData()
       }
   }
   ```

2. **Data Layer**
   ```kotlin
   // 1. Create data source
   class MyDataSource @Inject constructor()
   
   // 2. Implement repository
   class MyRepositoryImpl @Inject constructor(
       private val dataSource: MyDataSource
   ) : MyRepository {
       override suspend fun getData(): Result<MyData> {
           return try {
               val data = dataSource.fetch()
               Result.Success(data)
           } catch (e: Exception) {
               Result.Error(e.message ?: "Error")
           }
       }
   }
   ```

3. **Presentation Layer**
   ```kotlin
   // 1. Create UiState
   data class MyUiState(
       val isLoading: Boolean = false,
       val data: MyData? = null,
       val error: String? = null
   )
   
   // 2. Create ViewModel
   @HiltViewModel
   class MyViewModel @Inject constructor(
       private val getDataUseCase: GetDataUseCase
   ) : ViewModel()
   
   // 3. Create Screen
   @Composable
   fun MyScreen(viewModel: MyViewModel = hiltViewModel())
   ```

4. **Dependency Injection**
   ```kotlin
   @Module
   @InstallIn(SingletonComponent::class)
   abstract class MyModule {
       @Binds
       abstract fun bindMyRepository(
           impl: MyRepositoryImpl
       ): MyRepository
   }
   ```

## 🧪 Testing Guidelines

### Unit Tests

```kotlin
class MyViewModelTest {
    @Test
    fun `loadData success updates state`() = runTest {
        // Given
        val useCase = mockk<GetDataUseCase>()
        coEvery { useCase() } returns Result.Success(mockData)
        
        // When
        val viewModel = MyViewModel(useCase)
        viewModel.loadData()
        
        // Then
        assertEquals(mockData, viewModel.uiState.value.data)
    }
}
```

### Integration Tests

```kotlin
@Test
fun testDatabaseOperations() = runTest {
    // Given
    val dao = database.myDao()
    val entity = MyEntity(id = "1", name = "Test")
    
    // When
    dao.insert(entity)
    val result = dao.getById("1")
    
    // Then
    assertEquals(entity, result)
}
```

## 📚 Resources

- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Android Developers](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

## 💬 Questions?

- Open an [Issue](https://github.com/ronyaburaihan/NetSpeedIndicator/issues)
- Email: ronyaburaihan@gmail.com

## 🙏 Thank You!

Your contributions make this project better for everyone. Thank you for taking the time to contribute!

---

**Happy Coding! 🚀**
