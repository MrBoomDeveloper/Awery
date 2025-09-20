package com.mrboomdev.awery.extension.loaders

import androidx.core.net.toUri
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.context
import com.mrboomdev.awery.extension.loaders.yomi.YomiExtension
import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.ExtensionLoadException
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import kotlinx.coroutines.CancellationException
import ru.solrudev.ackpine.DelicateAckpineApi
import ru.solrudev.ackpine.installer.InstallFailure
import ru.solrudev.ackpine.installer.PackageInstaller
import ru.solrudev.ackpine.installer.createSession
import ru.solrudev.ackpine.installer.parameters.InstallPreapprovalDsl
import ru.solrudev.ackpine.installer.parameters.InstallerType
import ru.solrudev.ackpine.installer.parameters.PackageSource
import ru.solrudev.ackpine.session.Session
import ru.solrudev.ackpine.session.await
import ru.solrudev.ackpine.session.parameters.Confirmation
import java.io.File

internal actual suspend fun installImpl(managerId: String, file: PlatformFile): Extension {
	if(managerId == "yomi") {
		PackageInstaller.getInstance(Awery.context).createSession(
			when(val androidFile = file.androidFile) {
				is AndroidFile.UriWrapper -> androidFile.uri
				is AndroidFile.FileWrapper -> androidFile.file.toUri() 
			}
		) {
			confirmation = Confirmation.IMMEDIATE
			installerType = InstallerType.SESSION_BASED
			packageSource = PackageSource.Store
			requestUpdateOwnership = true
			
			@OptIn(DelicateAckpineApi::class)
			requireUserAction = false
		}.await().also { state ->
			when(val state = state) {
				is Session.State.Succeeded -> {
					val info = requireNotNull(
						Awery.context.packageManager.getPackageArchiveInfo(file.absolutePath(), 0)
					) { throw IllegalStateException("Failed to parse installed apk. Try restarting app.") }
					
					return YomiExtension(Awery.context.packageManager.getPackageInfo(
						info.packageName, Extensions.PACKAGE_MANAGER_FLAGS))
				}
				
				is Session.State.Failed -> {
					when(val failure = state.failure) {
						is InstallFailure.Aborted -> throw CancellationException(failure.message)
						
						is InstallFailure.Blocked -> throw ExtensionInstallException.Blocked(
							failure.otherPackageName ?: "your device")

						is InstallFailure.Incompatible -> throw ExtensionInstallException.Unsupported(
							failure.message ?: "Extension is unsupported on your device.")

						is InstallFailure.Invalid -> throw ExtensionInstallException.Invalid(
							failure.message ?: "Extension file is corrupted! Try installing it from a different source.")

						is InstallFailure.Storage -> throw ExtensionInstallException.Blocked(
							"your device", failure.message ?: "Not enough storage!")

						is InstallFailure.Timeout -> throw ExtensionInstallException.Blocked(
							"your device", failure.message ?: "Installation was too slow, so system has cancelled it.")
						
						is InstallFailure.Conflict -> throw ExtensionInstallException.Conflict()
						
						is InstallFailure.Generic -> throw ExtensionInstallException.Unknown(failure.message)
						
						else -> throw ExtensionInstallException.Unknown("NonExhaustiveWhenGuard")
					}
				}
			}
		}
	} else {
		throw UnsupportedOperationException("\"$managerId\" extension type isn't implemented yet!")
	}
}