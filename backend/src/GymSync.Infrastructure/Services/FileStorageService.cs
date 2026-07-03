using Amazon;
using Amazon.S3;
using Amazon.S3.Transfer;
using GymSync.Application.Common.Interfaces;
using Microsoft.Extensions.Configuration;

namespace GymSync.Infrastructure.Services;

public class FileStorageService : IFileStorageService
{
    private readonly IAmazonS3 _s3Client;
    private readonly string _bucketName;
    private readonly string _publicUrlBase;

    public FileStorageService(IConfiguration configuration)
    {
        var accessKey = configuration["R2:AccessKey"]!;
        var secretKey = configuration["R2:SecretKey"]!;
        var accountId = configuration["R2:AccountId"]!;
        _bucketName = configuration["R2:BucketName"]!;
        _publicUrlBase = configuration["R2:PublicUrlBase"]!;

        var config = new AmazonS3Config
        {
            RegionEndpoint = RegionEndpoint.USEast1,
            ServiceURL = $"https://{accountId}.r2.cloudflarestorage.com",
            ForcePathStyle = true
        };

        _s3Client = new AmazonS3Client(accessKey, secretKey, config);
    }

    public async Task<string> UploadFileAsync(Stream fileStream, string fileName, string contentType, CancellationToken ct = default)
    {
        var uploadRequest = new TransferUtilityUploadRequest
        {
            InputStream = fileStream,
            Key = fileName,
            BucketName = _bucketName,
            ContentType = contentType,
            CannedACL = S3CannedACL.PublicRead
        };

        var transferUtility = new TransferUtility(_s3Client);
        await transferUtility.UploadAsync(uploadRequest, ct);

        return $"{_publicUrlBase}/{fileName}";
    }

    public async Task<bool> DeleteFileAsync(string fileUrl, CancellationToken ct = default)
    {
        try
        {
            var key = fileUrl.Replace($"{_publicUrlBase}/", "");
            var deleteRequest = new Amazon.S3.Model.DeleteObjectRequest
            {
                BucketName = _bucketName,
                Key = key
            };

            var response = await _s3Client.DeleteObjectAsync(deleteRequest, ct);
            return response.HttpStatusCode == System.Net.HttpStatusCode.NoContent;
        }
        catch
        {
            return false;
        }
    }
}
