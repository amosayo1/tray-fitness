namespace GymSync.Application.Common.Interfaces;

public interface IFileStorageService
{
    Task<string> UploadFileAsync(Stream fileStream, string fileName, string contentType, CancellationToken ct = default);
    Task<bool> DeleteFileAsync(string fileUrl, CancellationToken ct = default);
}
