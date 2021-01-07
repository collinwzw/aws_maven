
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import java.util.Iterator;
import java.io.File;
import java.nio.file.Paths;

import java.util.List;

interface S3_Interface{
    Bucket getBucket(String bucketName);
    Bucket createBucket(String bucketName);
    void describeAllBuckets();
    List<Bucket> getAllBuckets();
    void deleteBucket(String bucketName);
    void putFileObject(String bucketName, String filePath);
    void describeAllObjectInBucket(String bucketName);
    void downloadObjectFromBucket(String bucketName, String fileName);

}
public class S3 implements S3_Interface {
    final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
    public S3(){}

    @Override
    public Bucket getBucket(String bucketName) {
        Bucket named_bucket = null;
        List<Bucket> buckets = this.getAllBuckets();
        for (Bucket b : buckets) {
            if (b.getName().equals(bucketName)) {
                named_bucket = b;
            }
        }
        return named_bucket;
    }

    @Override
    public Bucket createBucket(String bucketName) {
        Bucket b = null;
        if (s3.doesBucketExistV2(bucketName)) {
            System.out.format("Bucket %s already exists.\n", bucketName);
            b = this.getBucket(bucketName);
        } else {
            try {
                b = s3.createBucket(bucketName);
            } catch (AmazonS3Exception e) {
                System.err.println(e.getErrorMessage());
            }
        }
        return b;
    }

    @Override
    public void describeAllBuckets() {
        List<Bucket> buckets = this.getAllBuckets();
        for(Bucket bucket : buckets) {
            System.out.printf(
                    "Found s3 bucket with Name %s, " +
                            "Created Date %s, " +
                            "Owner %s, ",
                    bucket.getName(),
                    bucket.getCreationDate(),
                    bucket.getOwner());
            System.out.println();
        }
    }

    @Override
    public List<Bucket> getAllBuckets() {
        List<Bucket> buckets = s3.listBuckets();
        return buckets;
    }

    @Override
    public void deleteBucket(String bucketName) {
        try {
            System.out.println(" - removing objects from bucket: " + bucketName);
            ObjectListing object_listing = s3.listObjects(bucketName);
            while (true) {
                for (Iterator<?> iterator =
                     object_listing.getObjectSummaries().iterator();
                     iterator.hasNext(); ) {
                    S3ObjectSummary summary = (S3ObjectSummary) iterator.next();
                    s3.deleteObject(bucketName, summary.getKey());
                }

                // more object_listing to retrieve?
                if (object_listing.isTruncated()) {
                    object_listing = s3.listNextBatchOfObjects(object_listing);
                } else {
                    break;
                }
            }

            System.out.println(" - removing versions from bucket: " + bucketName);
            VersionListing version_listing = s3.listVersions(
                    new ListVersionsRequest().withBucketName(bucketName));
            while (true) {
                for (Iterator<?> iterator =
                     version_listing.getVersionSummaries().iterator();
                     iterator.hasNext(); ) {
                    S3VersionSummary vs = (S3VersionSummary) iterator.next();
                    s3.deleteVersion(
                            bucketName, vs.getKey(), vs.getVersionId());
                }

                if (version_listing.isTruncated()) {
                    version_listing = s3.listNextBatchOfVersions(
                            version_listing);
                } else {
                    break;
                }
            }

            System.out.println(" OK, bucket ready to delete!");
            s3.deleteBucket(bucketName);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Done!");
    }

    @Override
    public void putFileObject(String bucketName, String filePath) {
        String[] splits = filePath.split("/");
        String filename = splits[splits.length - 1];

        System.out.format("Uploading %s to S3 bucket %s...\n", filePath, bucketName);
        try {
            s3.putObject(bucketName, filename, new File(filePath));
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("File " + filename +" has been upload to s3 bucket " + bucketName);
    }

    @Override
    public void describeAllObjectInBucket(String bucketName) {

    }

    @Override
    public void downloadObjectFromBucket(String bucketName, String fileName) {

    }
}

class s3Demo{
    public static void main(String[] args) {
        S3 s3 = new S3();
        //List<Bucket> buckets = s3.getAllBuckets();
        String bucketName = "testziwen20210107";
        String file_path = "/c:/Users/ASUS/IdeaProjects/aws_maven/test.txt";
        s3.putFileObject(bucketName, file_path);
        //s3.createBucket(bucketName);
        //s3.describeAllBuckets();
        //s3.deleteBucket(bucketName);

    }
}
