
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;


interface EC2_Interface{
    public RunInstancesResult createInstance();
    public void describeAllInstances();
    public TerminateInstancesResult deleteInstance(String instanceID);
    public RebootInstancesResult rebootInstance(String instanceID);
}

public class EC2 implements EC2_Interface {
    private static final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();


    private static final String imageID = "ami-019d170c7f5dfa63c";
    private static final String securityGroupID = "sg-08912cd495e251bf9";
    private static final String keyName = "ece1779_demo";
    private static final String subnetID = "subnet-0fd0bf01";
    public EC2() {

    }

    @Override
    public RunInstancesResult createInstance() {
         RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
                .withImageId(imageID)
                .withInstanceType(InstanceType.T2Micro)
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(keyName)
                .withSecurityGroupIds(securityGroupID)
                .withSubnetId(subnetID);

        RunInstancesResult result = ec2.runInstances(runInstancesRequest);
        return result;
    }

    @Override
    public void describeAllInstances() {
        boolean done = false;

        DescribeInstancesRequest request = new DescribeInstancesRequest();
        while(!done) {
            DescribeInstancesResult response = ec2.describeInstances(request);

            for(Reservation reservation : response.getReservations()) {
                for(Instance instance : reservation.getInstances()) {
                    System.out.printf(
                            "Found instance with id %s, " +
                                    "AMI %s, " +
                                    "type %s, " +
                                    "state %s " +
                                    "and monitoring state %s",
                            instance.getInstanceId(),
                            instance.getImageId(),
                            instance.getInstanceType(),
                            instance.getState().getName(),
                            instance.getMonitoring().getState());
                }
                System.out.println();
            }

            request.setNextToken(response.getNextToken());

            if(response.getNextToken() == null) {
                done = true;
            }
        }
    }

    @Override
    public TerminateInstancesResult deleteInstance(String instanceID) {
        TerminateInstancesRequest deleteRequest = new TerminateInstancesRequest()
                .withInstanceIds(instanceID);
        TerminateInstancesResult deleteResponse = ec2.terminateInstances(deleteRequest);

        return deleteResponse;
    }

    @Override
    public RebootInstancesResult rebootInstance(String instanceID) {
        return null;
    }


}


class Demo{
    public static void main(String[] args) {
        EC2 ec2 = new EC2();
        ec2.describeAllInstances();

        //RunInstancesResult result = ec2.createInstance();
        //System.out.println(result);

        //TerminateInstancesResult result = ec2.deleteInstance("i-03e3db40c82ae5bf7");
        //System.out.println(result);
    }
}