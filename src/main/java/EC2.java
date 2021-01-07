import java.util.List;
import java.util.ArrayList;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;


interface EC2_Interface{
    RunInstancesResult createDefaultInstance();
    void describeAllInstances();
    List<Instance> getAllInstances();
    TerminateInstancesResult terminateInstance(String instanceID);
    RebootInstancesResult rebootInstance(String instanceID);
    List<Instance> filterInstanceByStatus(InstanceStateName state);
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
    public RunInstancesResult createDefaultInstance() {
         // create default instance with pre-defined AMIs, Instance Type, Name of Key Pair, Security Group ID and SubnetID
        // return the RunInstancesResult and also print out if the creation is success or not
         RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
                .withImageId(imageID)
                .withInstanceType(InstanceType.T2Micro)
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(keyName)
                .withSecurityGroupIds(securityGroupID)
                .withSubnetId(subnetID);

        RunInstancesResult result = ec2.runInstances(runInstancesRequest);
        if (result.getReservation().getInstances().get(0).getInstanceId() != null){
            System.out.println("EC2 instance with ID " + result.getReservation().getInstances().get(0).getInstanceId() + "has been successfully created.");
        }
        return result;

    }

    @Override
    public void describeAllInstances() {
        // Print out the detail information about all EC2 instances
        List<Instance> instances = this.getAllInstances();

        for(Instance instance : instances) {
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
            System.out.println();
        }

    }

    @Override
    public List<Instance> getAllInstances() {
        // get list of Instance object of all EC2 instances and return the list
        List<Instance> instances = new ArrayList<>();
        boolean done = false;

        DescribeInstancesRequest request = new DescribeInstancesRequest();
        while(!done) {
            DescribeInstancesResult response = ec2.describeInstances(request);

            for(Reservation reservation : response.getReservations()) {
                for(Instance instance : reservation.getInstances()) {
                    instances.add(instance);
                }
            }

            request.setNextToken(response.getNextToken());

            if(response.getNextToken() == null) {
                done = true;
            }
        }
        return instances;
    }

    @Override
    public TerminateInstancesResult terminateInstance(String instanceID) {
        // with given instance ID, terminate this instance
        TerminateInstancesRequest deleteRequest = new TerminateInstancesRequest()
                .withInstanceIds(instanceID);
        TerminateInstancesResult deleteResponse = ec2.terminateInstances(deleteRequest);

        return deleteResponse;
    }

    @Override
    public RebootInstancesResult rebootInstance(String instanceID) {
        //reboot the instance by using given instance ID
        RebootInstancesRequest request = new RebootInstancesRequest()
                .withInstanceIds(instanceID);

        RebootInstancesResult response = ec2.rebootInstances(request);

        System.out.printf(
                "Successfully rebooted instance %s", instanceID);
        return response;
    }

    @Override
    public List<Instance> filterInstanceByStatus(InstanceStateName state) {
        // filter all instances by given state
        List<Instance> instances = this.getAllInstances();
        List<Instance> filteredResult = new ArrayList<>();
        for(Instance instance : instances) {
            if(instance.getState().getName().equals(state.toString())){
                filteredResult.add(instance);
            }
        }
        return filteredResult;
    }


}


class ec2Demo{
    public static void main(String[] args) {
        EC2 ec2 = new EC2();
        ec2.describeAllInstances();

//        RunInstancesResult result = ec2.createDefaultInstance();
//        System.out.println(result);

        List<Instance> filteredResult = ec2.filterInstanceByStatus(InstanceStateName.Running);
        System.out.println(filteredResult);

//        RebootInstancesResult rebootResult = ec2.rebootInstance(filteredResult.get(0).getInstanceId());
//        System.out.println(rebootResult);
//        TerminateInstancesResult result = ec2.terminateInstance(filteredResult.get(0).getInstanceId());
//        System.out.println(result);
    }
}